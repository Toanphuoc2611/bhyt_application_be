package com.application.bhyt.controller;

import com.application.bhyt.dto.response.MyApiResponse;
import com.application.bhyt.service.BHYTService;
import com.application.bhyt.util.DateUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bhyt")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BHYTController {


}
