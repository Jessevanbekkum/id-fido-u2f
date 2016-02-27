package com.xebia.fido_u2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.google.common.base.Preconditions;
import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import com.yubico.u2f.data.messages.AuthenticateResponse;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;
import com.yubico.u2f.exceptions.DeviceCompromisedException;
import com.yubico.u2f.exceptions.NoEligableDevicesException;

@RestController
/**
 * This controller handles registration and logging in (the challenge) of users via several rest calls.
 */
public class ChallengeApi {
    private static final Logger LOG = LoggerFactory.getLogger(ChallengeApi.class);

    // Unique key for this web application.
    public static final String APP_ID = "https://localhost:8443";

    //Temporary store for active login processes
    private final Map<String, String> requestStorage = new HashMap<>();
    private final U2F u2f = new U2F();

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Returns a registration object, that will be used by the client FIDO U2F device to initialize itself for this webpage.
     *
     * @return
     */
    @RequestMapping(path = "yubi/register", method = RequestMethod.GET)
    public RegisterRequestData startRegistration() {
        LOG.info("Starting registration");
        String username = getUsernameFromAuthenticationContext();
        RegisterRequestData registerRequestData = u2f.startRegistration(APP_ID, getRegistrations(username));
        requestStorage.put(registerRequestData.getRequestId(), registerRequestData.toJson());
        return registerRequestData;
    }

    /**
     * Takes the result from the FIDO U2F device and stores that in the user database. This is the starting seed used for
     * future authentications.
     *
     * @param registerResponseStr
     * @return
     */
    @RequestMapping(path = "yubi/register", method = RequestMethod.POST,
            consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String finishRegistration(@RequestBody String registerResponseStr) {
        LOG.info("Finishing registration");
        String username = getUsernameFromAuthenticationContext();
        final RegisterResponse registerResponse = RegisterResponse.fromJson(registerResponseStr);
        RegisterRequestData registerRequestData =
                RegisterRequestData.fromJson(requestStorage.remove(registerResponse.getRequestId()));
        DeviceRegistration registration = u2f.finishRegistration(registerRequestData, registerResponse);
        storeRegistration(username, registration);
        return "{}";
    }

    /**
     * Start the authentication process. The server will generate a challenge, that must be correctly completed by the
     * FIDO U2F device
     * @return A challenge in JSON
     */
    @RequestMapping(value = "/yubi/auth", method = RequestMethod.GET)
    public ResponseEntity<String> startAuthentication(){
        LOG.info("Starting Authentication");
        String username = getUsernameFromAuthenticationContext();
        AuthenticateRequestData authenticateRequestData;
        try {
            authenticateRequestData = u2f.startAuthentication(APP_ID, getRegistrations(username));
        } catch (NoEligableDevicesException nede) {
            //A user does not have devices registered. User will see a message to register his device.
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        requestStorage.put(authenticateRequestData.getRequestId(), authenticateRequestData.toJson());
        return new ResponseEntity<>(authenticateRequestData.toJson(), HttpStatus.OK);
    }

    /**
     * Verify the returned challenge.
     * @param response
     * @return
     */
    @RequestMapping(value = "/yubi/auth", method = RequestMethod.POST)
    public ResponseEntity<String> finishAuthentication(@RequestBody String response) {
        LOG.info("Finishing Authentication");
        String username = getUsernameFromAuthenticationContext();

        AuthenticateResponse authenticateResponse = AuthenticateResponse.fromJson(response);
        AuthenticateRequestData authenticateRequest = AuthenticateRequestData.fromJson(requestStorage.remove(authenticateResponse.getRequestId()));
        DeviceRegistration registration = null;
        try {
            registration = u2f.finishAuthentication(authenticateRequest, authenticateResponse, getRegistrations(username));
        } catch (DeviceCompromisedException e) {
            registration = e.getDeviceRegistration();
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } finally {
            storeRegistration(username, registration);
        }

        grantAuthority();

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private String getUsernameFromAuthenticationContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }

    private Iterable<DeviceRegistration> getRegistrations(String username) {
        final User user = accountRepository.getUser(username);
        Preconditions.checkNotNull(user);
        List<DeviceRegistration> registrations = user.getRegistrations().stream()
                .map(DeviceRegistration::fromJson)
                .collect(Collectors.toList());
        return registrations;
    }

    private void storeRegistration(String username, DeviceRegistration registration) {
        accountRepository.getUser(username).putDevice(registration.getKeyHandle(), registration.toJson());
    }

    private void grantAuthority() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> authorities = new ArrayList<>(auth.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_STRONG_AUTH_USER"));
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

}

