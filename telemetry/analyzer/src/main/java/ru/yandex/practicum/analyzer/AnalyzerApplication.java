package ru.yandex.practicum.analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnalyzerApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(AnalyzerApplication.class, args);

        var hubProc = ctx.getBean(ru.yandex.practicum.analyzer.service.HubEventProcessor.class);
        var snapshotProc = ctx.getBean(ru.yandex.practicum.analyzer.service.SnapshotProcessor.class);

        Thread hubThread = new Thread(hubProc);
        hubThread.setName("HubEventHandlerThread");
        hubThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(hubThread::interrupt));

        snapshotProc.start();
    }
}