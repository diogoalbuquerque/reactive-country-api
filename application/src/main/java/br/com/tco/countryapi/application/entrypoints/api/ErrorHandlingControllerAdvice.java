package br.com.tco.countryapi.application.entrypoints.api;

import br.com.tco.countryapi.domain.exception.BaseDomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;

import static java.util.stream.Collectors.toList;

@ControllerAdvice
@RequiredArgsConstructor
public class ErrorHandlingControllerAdvice {
  protected static final int METHOD_ARGUMENT_ERROR_CODE = 99;
  private final MessageSource messageSource;

  @ExceptionHandler(BaseDomainException.class)
  public ResponseEntity<Object> handleBaseBusinessException(BaseDomainException ex) {
    var status = HttpStatus.BAD_REQUEST;
    var validationErrorResponse =
        new ValidationErrorResponse(
            status.value(), ex.getMessage(), ex.getErrorCode(), LocalDateTime.now());

    return ResponseEntity.badRequest().body(validationErrorResponse);
  }

  @ExceptionHandler(WebExchangeBindException.class)
  public ResponseEntity<Object> handleException(WebExchangeBindException ex) {

    var violations =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error -> {
                  var name = ((FieldError) error).getField();
                  var message = messageSource.getMessage(error, LocaleContextHolder.getLocale());
                  return new Violation(name, message);
                })
            .collect(toList());

    var validationErrorResponse =
        new ValidationErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage(),
            METHOD_ARGUMENT_ERROR_CODE,
            LocalDateTime.now(),
            violations);

    return ResponseEntity.badRequest().body(validationErrorResponse);
  }
}
