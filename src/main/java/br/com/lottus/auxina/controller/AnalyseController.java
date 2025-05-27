package br.com.lottus.auxina.controller;

import br.com.lottus.auxina.dto.TestResult;
import br.com.lottus.auxina.service.AnalyseService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/dev/analysis")
public class AnalyseController {

    private final AnalyseService analyseService;


    public AnalyseController(AnalyseService analyseService) {
        this.analyseService = analyseService;
    }

    @PostMapping("/trigger-book-test")
    public Mono<ResponseEntity<TestResult>> triggerLibraryPaginationTest() {
        return analyseService.performanceLibraryServicePaginationTest()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.status(500).build());
    }
}
