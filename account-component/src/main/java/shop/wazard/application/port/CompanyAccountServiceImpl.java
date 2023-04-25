package shop.wazard.application.port;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.wazard.application.port.in.CompanyAccountService;
import shop.wazard.application.port.out.LoadAccountPort;
import shop.wazard.application.port.out.SaveAccountPort;
import shop.wazard.dto.JoinReqDto;
import shop.wazard.dto.JoinResDto;
import shop.wazard.util.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
@Slf4j
class CompanyAccountServiceImpl implements CompanyAccountService {

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final JwtProvider jwtProvider;
    private final SaveAccountPort saveAccountPort;
    private final LoadAccountPort loadAccountPort;

    @Override
    @Transactional
    public JoinResDto join(JoinReqDto joinReqDto) {
        loadAccountPort.doubleCheckEmail(joinReqDto.getEmail());
        joinReqDto.setEncodedPassword(passwordEncoder.encode(joinReqDto.getPassword()));
        saveAccountPort.save(joinReqDto);
        return JoinResDto.builder()
                .message("회원가입에 성공하였습니다.")
                .build();
    }

}
