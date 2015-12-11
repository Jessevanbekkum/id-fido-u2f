package hello;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.yubico.u2f.data.messages.AuthenticateRequestData;
import com.yubico.u2f.data.messages.AuthenticateResponse;
import com.yubico.u2f.exceptions.DeviceCompromisedException;
import com.yubico.u2f.exceptions.NoEligableDevicesException;

@RestController
public class ChallengeApi {
    private final static Logger LOG = LoggerFactory.getLogger(ChallengeApi.class);

    Map<String, String> requestStorage = new HashMap<>();

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Path("/greeting")
    public String greeting() {
        return "Hallo";
    }

    @RequestMapping(value = "/yubi/register", method = RequestMethod.GET)
    public View startAuthentication(String username) throws NoEligableDevicesException {

        // Generate a challenge for each U2F device that this user has registered
        AuthenticateRequestData requestData
                = u2f.startAuthentication(SERVER_ADDRESS, getRegistrations(username));

        // Store the challenges for future reference
        requestStorage.put(requestData.getRequestId(), requestData.toJson());

        // Return an HTML page containing the challenges
        return new AuthenticationView(requestData.toJson(), username);
    }

    @RequestMapping(value = "/yubi/register", method = RequestMethod.POST)

    public String finishAuthentication(AuthenticateResponse response, String username) throws
            DeviceCompromisedException {

        // Get the challenges that we stored when starting the authentication
        AuthenticateRequestData authenticateRequest
                = requestStorage.remove(authenticateResponse.getRequestId());

        // Verify the that the given response is valid for one of the registered devices
        u2f.finishAuthentication(authenticateRequest,
                authenticateResponse,
                getRegistrations(username));

        return "Successfully authenticated!";
    }
}

