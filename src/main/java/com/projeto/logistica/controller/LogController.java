package com.projeto.logistica.controller;

import com.projeto.logistica.model.Log;
import com.projeto.logistica.repository.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @Autowired
    private LogRepository logRepository;

    @GetMapping
    public List<Log> listarLogs() {
        // Busca todos os logs que as Triggers criaram no banco
        return logRepository.findAll();
    }
}