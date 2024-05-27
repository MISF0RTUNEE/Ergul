package com.example.egrul.Controllers;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@RestController
public class MainController {
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/extract/{ogrn}")
    public ResponseEntity<Resource> getEgrul(@PathVariable String ogrn) throws IOException {

        //Первый запрос POST на url https://egrul.nalog.ru/, куда пеередаются данные
        // vyp3CaptchaToken:
        //page:
        //query: 1027700149124
        //region:
        //PreventChromeAutocomplete:
        // где query - наш передаваемый ОГРН

        // Содержимое данных формата x-www-form-urlencoded
        String postData = "vyp3CaptchaToken=&page=&query=" + ogrn + "&region=&PreventChromeAutocomplete=";
        // Устанавливаем заголовок Content-Type как application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // Создаем HttpEntity с данными и заголовками
        HttpEntity<String> request = new HttpEntity<>(postData, headers);
        // URL для отправки POST запроса
        String url = "https://egrul.nalog.ru/";
        // Отправляем POST запрос на сторонний сайт
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        //Записывем тело ответа
        String responseString = responseEntity.getBody();
        //В ответе получаем json {
        //    "t": "5F4D94E59CA6D4C3B4F88309DDF9463CDDE71FB3645F30C56DBC4229609FBE4D1FBC630F86F924037E42E4D1D11B996783789DC0CAFB0298FCE49A3337F9C0B1B90A15F0CE3173468EF1C65F98CB3CDD",
        //    "captchaRequired": false
        //}
        // где t - уникальный идентификатор


        // Получаем значение 't' из json
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> responseMap = mapper.readValue(responseString, new TypeReference<Map<String, Object>>() {
        });
        String tValue = (String) responseMap.get("t");

        //Второй запрос GET на url "https://egrul.nalog.ru/search-result/" + tValue + "?r=1716688853924&_=1716688853925"
        //где передается идентификатор из предыдущего запроса в переменной tValue
        String url1 = "https://egrul.nalog.ru/search-result/" + tValue + "?r=1716688853924&_=1716688853925";
        ResponseEntity<String> responseEntity1 = restTemplate.getForEntity(url1, String.class);
        //Получаем ответ в виде json
        //{
        //    "rows": [
        //        {
        //            "c": "ПАО \"МТС\"",
        //            "g": "ПРЕЗИДЕНТ: Николаев Вячеслав Константинович",
        //            "cnt": "1",
        //            "i": "7740000076",
        //            "k": "ul",
        //            "n": "ПУБЛИЧНОЕ АКЦИОНЕРНОЕ ОБЩЕСТВО \"МОБИЛЬНЫЕ ТЕЛЕСИСТЕМЫ\"",
        //            "o": "1027700149124",
        //            "p": "770901001",
        //            "r": "22.08.2002",
        //            "t": "C93BB1284E055F32F1CCC662CAFD58D86F86E0FDBF0C92F94A60B28013A4E14FC69DA06238881A434BEFFD1EC7ABBD94C4CE9C1645E6B0A3F6D1E4B0F6386867",
        //            "pg": "1",
        //            "tot": "1",
        //            "rn": "Г.Москва"
        //        }
        //    ]
        //}

        //Это результат поиска по выписке, здесь нам также нужен идентификатор         //            "t": "C93BB1284E055F32F1CCC662CAFD58D86F86E0FDBF0C92F94A60B28013A4E14FC69DA06238881A434BEFFD1EC7ABBD94C4CE9C1645E6B0A3F6D1E4B0F6386867",


        //Получаем t из json и записываем его в tValue1
        String responseString1 = responseEntity1.getBody();
        JsonNode responseNode = mapper.readTree(responseString1);
        JsonNode rowsNode = responseNode.get("rows");
        JsonNode firstRowNode = rowsNode.get(0);
        String tValue1 = firstRowNode.get("t").asText();




        // Третий запрос GET по по url "https://egrul.nalog.ru/vyp-download/" + tValue1 куда мы отправляем идентификатор из предыдущего запроса, а в ответ получаем pdf файл с выпиской
        String url2 = "https://egrul.nalog.ru/vyp-download/" + tValue1;
        byte[] fileContent = restTemplate.getForObject(url2, byte[].class);
        HttpHeaders headers1 = new HttpHeaders();
        headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=downloaded.pdf");
        return ResponseEntity.ok()
                .headers(headers1)
                .contentLength(fileContent.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(fileContent));
    }

}