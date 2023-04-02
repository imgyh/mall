package com.imgyh.mall.auth.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ClassName : GitHubOAuthInfo
 * @Package : com.imgyh.mall.auth.github
 * @Description :
 * @Author : imgyh
 * @Mail : admin@imgyh.com
 * @Github : https://github.com/imgyh
 * @Site : https://www.imgyh.com
 * @Date : 2023/4/2 14:03
 * @Version : v1.0
 * @ChangeLog :
 * * * * * * * * * * * * * * * * * * * * * * * *
 * <p>
 * * * * * * * * * * * * * * * * * * * * * * * *
 **/

@Component
@ConfigurationProperties(prefix = "oauth2.github")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GitHubOAuthInfo {
    private String clientId;

    private String clientSecrets;
}
