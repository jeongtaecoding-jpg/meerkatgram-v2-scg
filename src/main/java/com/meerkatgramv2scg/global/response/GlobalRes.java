package com.meerkatgramv2scg.global.response;

import com.meerkatgramv2scg.global.response.constant.CustomResponseCode;

public record GlobalRes<T>(
        String code
        , String message
        , T data
) {
    public static <T> GlobalRes<T> from(CustomResponseCode customResponseCode, T data) {
        return new GlobalRes<T>(customResponseCode.getCode(), customResponseCode.name(), data);

        // Static 메소드는 GlobalRes가 인스턴스되서 저장되는 영역과 별개로 저장된다.(정보가 다르다)
        // 따라서 제네릭은 별도로 저장해야 한다.
    }

    public static GlobalRes<Void> from(CustomResponseCode customResponseCode) {
        return new GlobalRes<Void> (customResponseCode.getCode(), customResponseCode.name(), null);
    }
        // customErrorCode 메소드의 파라미터 순서(code, name)는 바꾸면 안 된다.


    public static <T> GlobalRes<T> success(T data) {
        return GlobalRes.<T>from(CustomResponseCode.SUCCESS, data);
        // return new GlobalRes<T>(CustomResponseCode.SUCCESS.getCode(), CustomResponseCode.SUCCESS.name(), data); 이걸 윗줄로 바꿀 수 있다.
    }

      // data가 없는 success 패턴(GlobalRes)
    public static GlobalRes<Void> success() {
        return GlobalRes.<Void>from(CustomResponseCode.SUCCESS);
    }

      // data가 없는 success 패턴(ResponseEntity)
//    public static ResponseEntity<GlobalRes<Void>> success() {
//        return ResponseEntity.ok(new GlobalRes<Void>(CustomResponseCode.SUCCESS.getCode(), CustomResponseCode.SUCCESS.name(), null));
//    }

}
