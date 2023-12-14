package org.radarbase.management.service;




import org.radarbase.management.domain.User;
import org.radarbase.management.web.rest.AccountResource;
import org.radarbase.management.web.rest.errors.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.herc.common.security.mf.MfOptSender;

import static org.radarbase.management.web.rest.errors.EntityName.USER;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_EMAIL_NOT_REGISTERED;

@Service("mfOptSender")
public class MfOptSenderImpl  implements MfOptSender {
    private static final Logger log = LoggerFactory.getLogger(MfOptSenderImpl.class);
    @Autowired
    private MailService mailService;

    @Autowired
    private UserService userService;
    @Override
    public void sendUserOpt(String userName, String otp) {
        String email = userService.getUserEmail(userName) ;
        mailService.sendEmail(email, "Connect OTP", otp, false, false);
    }
}
