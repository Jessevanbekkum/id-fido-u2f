package hello;

import java.util.*;
import javax.websocket.server.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yubico.u2f.U2F;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.RegisterRequestData;
import com.yubico.u2f.data.messages.RegisterResponse;

@RestController
public class ChallengeApi {
    private final static Logger LOG = LoggerFactory.getLogger(ChallengeApi.class);
    public static final String APP_ID = "https://localhost:8443";

    Map<String, String> requestStorage = new HashMap<>();
    List<RegisterRequestData> challengeStore = new ArrayList<>();
    private final U2F u2f = new U2F();

    private final LoadingCache<String, Map<String, String>> userStorage = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, String>>() {
        @Override
        public Map<String, String> load(String key) throws Exception {
            return new HashMap<>();
        }
    });

    @RequestMapping(path = "yubi/register/{username}", method = RequestMethod.GET
    )
    public RegisterRequestData startRegistration(@PathVariable String username) {
        final RegisterRequestData challenge = u2f.startRegistration(APP_ID, Collections.emptyList());
        challengeStore.add(challenge);

        return challenge;

    }

    @RequestMapping(path = "yubi/register/{username}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String finishRegistration(@RequestBody String registerResponseStr, @PathParam("username") String username) {
        System.out.println(registerResponseStr);

        final RegisterResponse registerResponse = RegisterResponse.fromJson(registerResponseStr);


        RegisterRequestData registerRequestData =
                RegisterRequestData.fromJson(requestStorage.remove(registerResponse.getRequestId()));
        DeviceRegistration registration = u2f.finishRegistration(registerRequestData, registerResponse);
        addRegistration(username, registration);
        return "{}";
    }

//
//    @RequestMapping(value = "/yubi/auth", method = RequestMethod.GET)
//    public String startAuthentication(String username) throws NoEligableDevicesException {
//
//        // Generate a challenge for each U2F device that this user has registered
//        AuthenticateRequestData requestData
//                = u2f.startAuthentication(SERVER_ADDRESS, getRegistrations(username));
//
//        // Store the challenges for future reference
//        requestStorage.put(requestData.getRequestId(), requestData.toJson());
//
//        // Return an HTML page containing the challenges
//        return requestData.toJson();
//    }
//
//    @RequestMapping(value = "/yubi/auth", method = RequestMethod.POST)
//
//    public String finishAuthentication(AuthenticateResponse authenticateResponse, String username) throws
//            DeviceCompromisedException {
//
//        // Get the challenges that we stored when starting the authentication
//        AuthenticateRequestData authenticateRequest
//                = requestStorage.remove(authenticateResponse.getRequestId());
//
//        // Verify the that the given response is valid for one of the registered devices
//        u2f.finishAuthentication(authenticateRequest,
//                authenticateResponse,
//                getRegistrations(username));
//
//        return "Successfully authenticated!";
//    }

    private void addRegistration(String username, DeviceRegistration registration) {
        userStorage.getUnchecked(username).put(registration.getKeyHandle(), registration.toJson());
    }
}

