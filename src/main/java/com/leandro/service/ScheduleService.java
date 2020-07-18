package com.leandro.service;

import com.leandro.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ScheduleService {

    private static final String CRON_LATE_LOANS = "0 0/2 * 1/1 * ?";

    @Value("${application.mail.lateLoans.message}")
    private String message;

    private final LoanService loanService;
    private final EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendMailToLateLoans() {
        List<Loan> lateLoans = loanService.getAllLateLoans();
        List<String> emailList = lateLoans.stream()
                .map(Loan::getCustomerEmail)
                .collect(Collectors.toList());
        if(!emailList.isEmpty()) {
            log.info("Email sendo enviado");
            emailService.sendMails(message, emailList);
        } else {
            log.info("Não há livros atrasados.");
        }

    }
}
