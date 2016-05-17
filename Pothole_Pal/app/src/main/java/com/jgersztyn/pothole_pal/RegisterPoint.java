package com.jgersztyn.pothole_pal;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class RegisterPoint extends StringRequest {

    private static final String REGISTER_REQUEST_URL = "add valid url";
    private Map<String, String> params;

    public RegisterPoint(String text, String position, Response.Listener<String> listener) {
        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("text", text);
        params.put("position", position);
    }
}

