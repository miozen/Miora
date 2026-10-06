package liuyuyang.net.core.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import liuyuyang.net.model.User;
import liuyuyang.net.web.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
    private static final int MINIMUM_PASSWORD_LENGTH = 12;
    @Resource private UserService userService;
    @Value("$" + "{BOOTSTRAP_ADMIN_USERNAME:}") private String username;
    @Value("$" + "{BOOTSTRAP_ADMIN_PASSWORD:}") private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userService.count(new LambdaQueryWrapper<User>()) > 0) return;
        if (username == null || username.trim().isEmpty()) throw new IllegalStateException("BOOTSTRAP_ADMIN_USERNAME must be set for an empty Miora database.");
        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters for an empty Miora database.");
        User admin = new User();
        admin.setUsername(username.trim());
        admin.setPassword(DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8)));
        admin.setName("Miora administrator");
        admin.setInfo("Initial Miora administrator");
        admin.setCreateTime(System.currentTimeMillis());
        userService.save(admin);
    }
}
