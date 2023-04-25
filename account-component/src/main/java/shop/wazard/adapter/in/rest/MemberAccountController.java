package shop.wazard.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wazard.adapter.in.rest.request.JoinReq;
import shop.wazard.application.port.in.MemberAccountService;
import shop.wazard.dto.JoinReqDto;
import shop.wazard.dto.JoinResDto;
import shop.wazard.util.response.Message;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account/members")
class MemberAccountController {

    private final ControllerConverter controllerConverter;
    private final MemberAccountService memberAccountService;

    @PostMapping("/join")
    public ResponseEntity<Message> join(@Valid @RequestBody JoinReq joinreq) {
        JoinReqDto joinReqDto = controllerConverter.joinReqToJoinReqDto(joinreq);
        JoinResDto joinResDto = memberAccountService.join(joinReqDto);
        return ResponseEntity.ok(
                Message.builder()
                        .httpStatus(HttpStatus.OK)
                        .result(controllerConverter.joinResDtoToJoinRes(joinResDto))
                        .build()
        );
    }

}
