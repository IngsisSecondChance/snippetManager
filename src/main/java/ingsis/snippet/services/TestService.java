package ingsis.snippet.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ingsis.snippet.dto.Response;
import ingsis.snippet.dto.TestDTO;
import ingsis.snippet.entities.Snippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ingsis.snippet.web.BucketHandler;
import ingsis.snippet.web.PermissionsManagerHandler;
import ingsis.snippet.web.PrintScriptServiceHandler;
import ingsis.snippet.entities.Test;
import ingsis.snippet.errorDTO.Error;
import ingsis.snippet.repositories.SnippetRepository;
import ingsis.snippet.repositories.TestRepository;



import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import static ingsis.snippet.utils.Utils.getViolationsMessageError;

@Service
public class TestService {

    private static final Logger log = LoggerFactory.getLogger(TestService.class);
    @Autowired
    private TestRepository testRepository;

    @Autowired
    private SnippetRepository snippetRepository;

    @Autowired
    private BucketHandler bucketHandler;

    @Autowired
    private PermissionsManagerHandler permissionsManagerHandler;

    @Autowired
    private PrintScriptServiceHandler printScriptServiceHandler;

    private final Validator validation = Validation.buildDefaultValidatorFactory().getValidator();

    public Response<String> createTest(TestDTO testDTO, String token) {
        log.info("createTest was called");
        Set<ConstraintViolation<TestDTO>> violations = validation.validate(testDTO);
        if (!violations.isEmpty()) {
            return Response.withError(getViolationsMessageError(violations));
        }

        String snippetId = testDTO.getId();

        Response<String> permissionsResponse = permissionsManagerHandler.checkPermissions(snippetId, token,
                "/snippets/can-edit");
        if (permissionsResponse.isError())
            return permissionsResponse;

        Optional<Snippet> snippet = snippetRepository.findById(snippetId);
        if (snippet.isEmpty()) {
            return Response.withError(new Error<>(404, "Snippet not found"));
        }

        Test test = new Test();
        if (snippet.get().getTests().stream().map(Test::getTitle).toList().contains(testDTO.getTitle())) {
            test = snippet.get().getTests().stream().filter(t -> t.getTitle().equals(testDTO.getTitle())).findFirst()
                    .get();
        }

        test.setTitle(testDTO.getTitle());
        test.setSnippet(snippet.get());
        test.setInputs(
                testDTO.getInputQueue().stream().map(input -> "(" + input + ")").collect(Collectors.joining("(*)")));
        test.setExpectedOutputs(
                testDTO.getOutputQueue().stream().map(output -> "(" + output + ")").collect(Collectors.joining("(*)")));

        try {
            testRepository.save(test);
        } catch (Exception e) {
            return Response.withError(new Error<>(500, e.getMessage()));
        }

        return Response.withData(test.getId());
    }

}