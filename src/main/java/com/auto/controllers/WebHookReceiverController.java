package com.auto.controllers;

import com.auto.utils.ShellScriptUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.Objects;


@Controller
public class WebHookReceiverController {


    @Value("${github.user.agent.prefix}")
    private String githubUserAgentPrefix;

    private String secretKey;

    @Value("${github.publish.message.hint}")
    private String publishMessageHint;

    @Value("${application.version}")
    private String appVersion;

    @Autowired
    private Environment environment;

    public WebHookReceiverController(){
        this(System.getenv("GHSecretKey"));
    }

    public WebHookReceiverController(String secretKey) {
        this.secretKey = secretKey;
        Objects.requireNonNull(secretKey, "Github secret key is required");
    }

    @PostMapping("/push")
    public ResponseEntity<String> receive(@RequestHeader("X-Hub-Signature") String signature, @RequestBody String payload,
                                  @RequestHeader("User-Agent") String userAgent){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Github-Webhook-Client-Version", "appVersion");

        if (Objects.isNull(userAgent) || !userAgent.startsWith(githubUserAgentPrefix)){
            return new ResponseEntity<>("Invalid request", httpHeaders, HttpStatus.BAD_REQUEST);
        }

        if (signature == null){
            return new ResponseEntity<>("Invalid signature", httpHeaders, HttpStatus.BAD_REQUEST);
        }

        String computed = String.format("sha1=%s", HmacUtils.hmacSha1Hex(secretKey, payload));

        if (!MessageDigest.isEqual(signature.getBytes(), computed.getBytes())){
            return new ResponseEntity<String>("Invalid signature", httpHeaders, HttpStatus.UNAUTHORIZED);
        }

        Map<?,?> repo;

        try {
            Map<?,?> payloadMap = new ObjectMapper().readValue(payload, Map.class);
            repo = (Map<?, ?>) payloadMap.get("repository");

            String repoName = (String) repo.get("full_name");
            String repoKey = "REPO_"+ repoName.replace("/", "_").toUpperCase() + "_SHELL";

            if (Objects.nonNull(environment)){
                String shellPath = environment.getProperty(repoKey);
                if (shellPath != null && new File(shellPath).exists()){
                    if (!Objects.isNull(publishMessageHint) && !publishMessageHint.isEmpty()){
                        if (payloadMap.containsKey("head_commit")){
                            String message = (String) ((Map<?,?>) payloadMap.get("head_commit")).get("message");
                            if (!Objects.isNull(message) && !message.isEmpty() && message.contains(publishMessageHint)){
                                callShellScript(shellPath);
                            }else {
                                System.out.println("Publish Message hint not found in message, not creating trigger file");
                            }
                        }
                    }else {
                        System.out.println("Publish Message hint is not specified. Call shell script");
                        callShellScript(shellPath);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Unable to parse response.", httpHeaders, HttpStatus.BAD_REQUEST);
        }

        int bytes = payload.getBytes().length;
        StringBuilder response = new StringBuilder();
        response.append("Signature verified");
        response.append(String.format("Received %d bytes.", bytes));
        response.append(String.format("Github WebHook client version - %s", appVersion));
        return new ResponseEntity<>(response.toString(), httpHeaders, HttpStatus.OK);
    }

    private void callShellScript(String shellPath) {
        ShellScriptUtil.execute(shellPath);
    }
}
