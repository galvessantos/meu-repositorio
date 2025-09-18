package com.montreal.core.exception_handler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.montreal.core.domain.exception.CheckImageException;
import com.montreal.core.domain.exception.ClientServiceException;
import com.montreal.core.domain.exception.ConflictException;
import com.montreal.core.domain.exception.ConflictUserException;
import com.montreal.core.domain.exception.CryptoException;
import com.montreal.core.domain.exception.EmailException;
import com.montreal.core.domain.exception.ImageNotAllowedException;
import com.montreal.core.domain.exception.InternalErrorException;
import com.montreal.core.domain.exception.NegocioException;
import com.montreal.core.domain.exception.NotFoundException;
import com.montreal.core.domain.exception.SgdBrokenException;
import com.montreal.core.domain.exception.TokenAccessException;
import com.montreal.core.domain.exception.UnauthorizedException;
import com.montreal.oauth.domain.exception.AuthenticateException;
import com.montreal.oauth.domain.exception.DuplicateUserException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler  extends ResponseEntityExceptionHandler {

    public static final String MSG_ERROR_GENERIC = "Ocorreu um erro interno inesperado no sistema. Tente novamente e se o problema persistir, entre em contato com o administrador do sistema.";
    public static final String MSG_ERROR_TOKEN_EXPIRED = "Seu token de autenticação expirou. Por favor, faça login novamente.";

    private final MessageSource messageSource;
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<?> handleConflictException(ConflictException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ProblemType problemType = ex.getProblemType();
        String detail = ex.getReason();

        Problem problem = createProblemBuilder(status, problemType, detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ProblemType problemType = ex.getProblemType();
        String detail = ex.getReason();

        Problem problem = createProblemBuilder(status, problemType, detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }


    @ExceptionHandler({SgdBrokenException.class})
    public ResponseEntity<?> handleSgdBrokenException(SgdBrokenException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({TokenAccessException.class})
    public ResponseEntity<?> handleTokenAccessException(TokenAccessException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({AuthenticateException.class})
    public ResponseEntity<?> handleAuthenticateException(AuthenticateException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemType problemType = ProblemType.ACESSO_NEGADO;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({BadCredentialsException.class})
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemType problemType = ProblemType.DADOS_INVALIDOS;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<?> handleUnauthorizedException(BadCredentialsException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        ProblemType problemType = ProblemType.TOKEN_INVALIDOS;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(CryptoException.class)
    public ResponseEntity<?> handleCryptoException(CryptoException ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(InternalErrorException.class)
    public ResponseEntity<?> handleUncaught(InternalErrorException ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;

        Problem problem = createProblemBuilder(status, problemType, MSG_ERROR_GENERIC).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(EmailException.class)
    public ResponseEntity<?> handleEmailException(EmailException ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<?> handleNegocio(NegocioException ex, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ProblemType problemType = ex.getProblemType();
        String detail = ex.getReason();

        Problem problem = createProblemBuilder(status, problemType, detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(CheckImageException.class)
    public ResponseEntity<?> handleCheckImageException(CheckImageException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ProblemType problemType = ProblemType.ERRO_NEGOCIO;
        String detail = ex.getMessage();

        var problem = createProblemBuilder(status, problemType, detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(ConflictUserException.class)
    public ResponseEntity<?> handleConflictUserException(ConflictUserException ex, WebRequest request) {

        HttpStatus status = HttpStatus.CONFLICT;
        ProblemType problemType = ProblemType.ENTIDADE_EM_USO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);

    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<?> handleDuplicateUserException(DuplicateUserException ex, WebRequest request) {

        HttpStatus status = HttpStatus.CONFLICT;
        ProblemType problemType = ProblemType.ENTIDADE_EM_USO;
        String detail = ex.getMessage();

        Problem problem = createProblemBuilder(status, problemType, detail).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);

    }

    @ExceptionHandler(ImageNotAllowedException.class)
    public ResponseEntity<?> handleImageNotAllowedException(ImageNotAllowedException ex, WebRequest request) {
        log.error("[ImageNotAllowedException] Error {}", ex.getMessage(), ex);
        HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        ProblemType problemType = ProblemType.PARAMETRO_INVALIDO;
        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();
        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);
    }

    @ExceptionHandler(ClientServiceException.class)
    public ResponseEntity<?> handleClientServiceException(ClientServiceException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_GATEWAY;
        ProblemType problemType = ProblemType.ERRO_SISTEMA_LATERAL;

        Problem problem = createProblemBuilder(status, problemType, ex.getMessage()).build();

        return handleExceptionInternal(ex, problem, new HttpHeaders(), status, request);

    }
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";

        BindingResult bindingResult = ex.getBindingResult();

        List<Problem.Object> problemObjects = bindingResult.getAllErrors().stream()
                .map(objectError -> {
                    String message = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());

                    String name = objectError.getObjectName();

                    if (objectError instanceof FieldError) {
                        name = ((FieldError) objectError).getField();
                    }

                    return Problem.Object.builder()
                            .name(name)
                            .userMessage(message)
                            .build();
                })
                .collect(Collectors.toList());

        Problem problem = createProblemBuilder(HttpStatus.valueOf(status.value()), problemType, detail)
                .objects(problemObjects)
                .userMessage("Um ou mais campos estão inválidos. Corrija os erros e tente novamente.")
                .build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }


//    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
//        ProblemType problemType = ProblemType.MENSAGEM_INCOMPREENSIVEL;
//        String detail = "O corpo da requisição está inválido. Verifique erro de sintaxe.";
//
//        Problem problem = createProblemBuilder(status, problemType, detail).build();
//
//        return handleExceptionInternal(ex, problem, headers, status, request);
//    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        HttpStatus statusEnum = HttpStatus.valueOf(status.value());

        ProblemType problemType = ProblemType.PARAMETRO_INVALIDO;
        String detail = "O corpo da requisição está inválido. Verifique erro de sintaxe ou valores inválidos como enumeração.";

        Problem problem = createProblemBuilder(statusEnum, problemType, detail)
                .userMessage("Verifique os dados enviados. Campos obrigatórios ou valores inválidos podem estar incorretos.")
                .build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ProblemType problemType = ProblemType.RECURSO_NAO_ENCONTRADO;
        String detail = String.format("O recurso %s, que você tentou acessar, é inexistente.", ex.getRequestURL());

        Problem problem = createProblemBuilder(status, problemType, detail)
                .userMessage("O recurso solicitado não foi encontrado. Verifique o endereço e tente novamente.")
                .build();

        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    private Problem.ProblemBuilder createProblemBuilder(HttpStatus status, ProblemType problemType, String detail) {
        return Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .type(problemType.getUri())
                .title(problemType.getTitle())
                .detail(detail);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (body == null) {
            body = Problem.builder()
                    .timestamp(OffsetDateTime.now())
                    .status(status.value())
                    .title(status.getReasonPhrase())
                    .userMessage("Ocorreu um erro inesperado. Tente novamente mais tarde.")
                    .build();
        } else if (body instanceof String) {
            body = Problem.builder()
                    .timestamp(OffsetDateTime.now())
                    .status(status.value())
                    .title((String) body)
                    .userMessage("Ocorreu um erro inesperado. Tente novamente mais tarde.")
                    .build();
        }
        return new ResponseEntity<>(body, headers, status);
    }
    
    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            org.springframework.beans.TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemType problemType = ProblemType.DADOS_INVALIDOS;
        String detail = String.format("Valor inválido para o parâmetro '%s'. %s",
                ex.getPropertyName(), ex.getMessage());

        Problem problem = createProblemBuilder(HttpStatus.BAD_REQUEST, problemType, detail)
                .userMessage("Um ou mais parâmetros da requisição estão com valor inválido.")
                .build();

        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }
    

	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex, WebRequest request) {
	    String fieldName = ex.getPath().isEmpty() ? "campo" : ex.getPath().get(0).getFieldName();
	    Class<?> targetType = ex.getTargetType();
	
	    // Mensagens personalizadas para enums
	    String detail;
	    if (targetType.isEnum()) {
	        if ("vehicleCondition".equals(fieldName)) {
	            detail = "A condição do veículo é obrigatória.";
	        } else if ("seizureStatus".equals(fieldName)) {
	            detail = "O status da apreensão é obrigatório.";
	        } else {
	            detail = String.format("Valor inválido para o campo '%s'. Informe um dos valores válidos: %s", fieldName, enumValues(targetType));
	        }
	    } else {
	        detail = String.format("Formato inválido para o campo '%s'.", fieldName);
	    }
	
	    ProblemType problemType = ProblemType.PARAMETRO_INVALIDO;
	    Problem problem = createProblemBuilder(HttpStatus.BAD_REQUEST, problemType, detail)
	            .userMessage(detail)
	            .build();
	
	    return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	private String enumValues(Class<?> enumType) {
	    Object[] constants = enumType.getEnumConstants();
	    return constants != null
	            ? String.join(", ", 
	                java.util.Arrays.stream(constants).map(Object::toString).toList()
	              )
	            : "";
	}


}
