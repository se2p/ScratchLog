/*
 * Copyright (C) 2023 ScratchLog contributors
 *
 * This file is part of ScratchLog.
 *
 * ScratchLog is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * ScratchLog is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ScratchLog. If not, see <http://www.gnu.org/licenses/>.
 */
package fim.unipassau.de.scratchLog.application;

import fim.unipassau.de.scratchLog.application.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine springTemplateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    private static final String HTML_BODY = "message";
    private static final String TO = "gordon.fraser@uni-passau.de";
    private static final String CC = "MoreUnicorns!";
    private static final String BCC = "UnicornsRule!";
    private static final String REPLY_TO = "JustIgnoreThis";
    private static final String SUBJECT = "Unicorns";
    private static final String TEMPLATE = "unicorns";
    private static final Map<String, Object> MODEL = new HashMap<>();

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        Field from = mailService.getClass().getDeclaredField("email");
        from.setAccessible(true);
        from.set(mailService, "unicorns@unicorns.com");
    }

    @Test
    public void testSendEmail() {
        when(springTemplateEngine.process(anyString(), any())).thenReturn(HTML_BODY);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        assertTrue(mailService.sendEmail(TO, SUBJECT, MODEL, TEMPLATE));
        verify(springTemplateEngine).process(anyString(), any());
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    public void testSendTemplateMessage() throws MessagingException {
        when(springTemplateEngine.process(anyString(), any())).thenReturn(HTML_BODY);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        mailService.sendTemplateMessage(TO, CC, BCC, REPLY_TO, SUBJECT, MODEL, TEMPLATE);
        verify(springTemplateEngine).process(anyString(), any());
        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(any(MimeMessage.class));
    }
}
