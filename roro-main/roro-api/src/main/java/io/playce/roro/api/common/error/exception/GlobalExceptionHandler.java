/*
 * Copyright 2020 The Playce-RoRo Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision History
 * Author			Date				Description
 * ---------------	----------------	------------
 * Jeongho Baek     10월 21, 2020       First Draft.
 */
package io.playce.roro.api.common.error.exception;

import io.playce.roro.api.common.error.ErrorCode;
import io.playce.roro.api.common.error.ErrorResponse;
import io.playce.roro.api.common.i18n.LocaleMessageConvert;
import io.playce.roro.api.domain.authentication.jwt.JwtTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/**
 * <pre>
 *   전역으로 처리할 Exception을 처리하는 클래스이다.
 * </pre>
 *
 * @author Jeongho Baek
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * The Locale message convert.
     */
    private final LocaleMessageConvert localeMessageConvert;

    /**
     * Instantiates a new Global exception handler.
     *
     * @param localeMessageConvert the locale message convert
     */
    public GlobalExceptionHandler(LocaleMessageConvert localeMessageConvert) {
        this.localeMessageConvert = localeMessageConvert;
    }

    /**
     * Valid or Validated 으로 binding error 발생시 발생한다. HttpMessageConverter 에서 등록한 HttpMessageConverter
     * binding 못할경우 발생 주로 @RequestBody, @RequestPart 어노테이션에서 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, WebRequest request) {
        log.error("MethodArgumentNotValidException occurred while execute [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.INVALID_INPUT_VALUE));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, WebRequest request) {
        log.error("MethodNotSupportedException occurred while execute [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.METHOD_NOT_ALLOWED));
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 지원하지 않은 Content-Type일 경우 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException e, WebRequest request) {
        log.error("HttpMediaTypeNotSupportedException occurred while execute [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(), e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Login 및 Authentication, JWT Parse 및 Validation 할 때 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler({JwtTokenException.class, AuthenticationException.class})
    protected ResponseEntity<ErrorResponse> handleAuthenticationException(Exception e, WebRequest request) {
        // ignore AUTH_HEADER_BLANK(AUTH_004) & EXPIRED_JWT(AUTH_008) code
        if (!e.getMessage().contains(ErrorCode.AUTH_HEADER_BLANK.getCode()) && !e.getMessage().contains(ErrorCode.EXPIRED_JWT.getCode())) {
            log.error("AuthenticationException occurred while execute [{}].", request.getDescription(false), e);
        }

        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(e.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Login 실패 할 때 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    protected ResponseEntity<ErrorResponse> handleLoginFailException(Exception e, WebRequest request) {
        // ignore AUTH_HEADER_BLANK(AUTH_004) & EXPIRED_JWT(AUTH_008) code
        if (!e.getMessage().contains(ErrorCode.AUTH_HEADER_BLANK.getCode()) && !e.getMessage().contains(ErrorCode.EXPIRED_JWT.getCode())) {
            log.error("AuthenticationException occurred while execute [{}].", request.getDescription(false), e);
        }

        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(e.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * 주어진 Entity를 참조하는 rows가 있는 경우 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler(ResourceInUseException.class)
    protected ResponseEntity<ErrorResponse> handleResourceInUseException(ResourceInUseException e, WebRequest request) {
        log.error("ResourceInUseException occurred while execute [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.RESOURCE_IN_USE));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * 주어진 Entity id에 해당하는 Row가 없는 경우 발생
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e, WebRequest request) {
        log.error("ResourceNotFoundException occurred while execute [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.RESOURCE_NOT_FOUND, e.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * database unique key 위배시.
     *
     * @param e
     * @param request
     *
     * @return
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ErrorResponse> handleException(DataIntegrityViolationException e, WebRequest request) {
        log.error("DataIntegrityViolationException [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.CONSTRAINT_VIOLATION, getCausedException(e).getMessage()));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RoRoApiException.class)
    protected ResponseEntity<ErrorResponse> handleRoRoApiException(RoRoApiException e, WebRequest request) {
        log.error("RoRoException occurred while execute [{}]. Message : [{}]",
                request.getDescription(false), localeMessageConvert.getMessage(e.getErrorCode().getMessage(), e.getParameters()), e);

        ErrorResponse response;

        if (e.getErrorCode().getMessage().equals(e.getMessage())) {
            response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(e.getErrorCode()));
        } else {
            response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(e.getErrorCode(), e.getParameters()));
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Argument 관련 에러 발생시
     *
     * @param e
     * @param request
     *
     * @return
     */
    @ExceptionHandler({IllegalArgumentException.class, MissingRequestValueException.class, MissingServletRequestPartException.class})
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(Exception e, WebRequest request) {
        log.error("IllegalArgumentException [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.ILLEGAL_ARGUMENT, e.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle exception response entity.
     *
     * @param e       the e
     * @param request the request
     *
     * @return the response entity
     */
    @ExceptionHandler({Exception.class, RuntimeException.class})
    protected ResponseEntity<ErrorResponse> handleException(Exception e, WebRequest request) {
        log.error("Unhandled exception occurred while execute [{}].", request.getDescription(false), e);
        ErrorResponse response = ErrorResponse.of(localeMessageConvert.getConvertErrorMessage(ErrorCode.INTERNAL_SERVER_ERROR));
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Exception getCausedException(Exception e) {
        if (e.getCause() != null && e.getCause() instanceof Exception) {
            return getCausedException((Exception) e.getCause());
        }

        return e;
    }
}
//end of GlobalExceptionHandler.java