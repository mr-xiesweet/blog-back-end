package top.xie;

import io.jsonwebtoken.Claims;
import top.xie.utils.JwtUtil;

public class TestToken {

    public static void main(String[] args) {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX25hbWUiOiLnlKjmiLfms6jlhozlkI0iLCJyb2xlcyI6InJvbGVfbm9ybWFsIiwic2lnbiI6bnVsbCwiaWQiOiI3ODE0NTg4NTExMDE2NzE0MjQiLCJhdmF0YXIiOiJodHRwczovL2Nkbi5zdW5vZmJlYWNoZXMuY29tL2ltYWdlcy9kZWZhdWx0X2F2YXRhci5wbmciLCJleHAiOjE2MDYzODA2MzUsImVtYWlsIjoiMTA5NTY1ODAzNEBxcS5jb20ifQ.5saWIZOYQMZfcBI_YdsYp_EF3N08epAFW7KuhhNhjBs";
        Claims claims = JwtUtil.parseJWT(token);
    }
}
