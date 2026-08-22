package com.application.bhyt.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum LoaiBHYT {
    GIA_HAN("Gia hạn"),
    MOI("Mới")
    ;
    String loai;
}
