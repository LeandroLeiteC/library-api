package com.leandro.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailService {

    void sendMails(String message, List<String> emailList);
}
