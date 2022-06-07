package br.com.tco.countryapi.application.entrypoints.api.country;

import br.com.tco.countryapi.application.entrypoints.api.country.dto.CountryRequest;
import br.com.tco.countryapi.application.entrypoints.api.country.dto.CountryResponse;
import br.com.tco.countryapi.domain.country.entities.Country;
import br.com.tco.countryapi.domain.country.usecase.FindCountry;
import br.com.tco.countryapi.domain.country.usecase.SaveCountry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/country")
@RequiredArgsConstructor
public class CountryController {
  final FindCountry findCountry;
  final SaveCountry saveCountry;

  @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
  public Publisher<ResponseEntity<CountryResponse>> get(@PathVariable String id) {
    return findCountry
        .findById(id)
        .map(country -> ResponseEntity.ok(new CountryResponse(country)))
        .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @GetMapping(produces = APPLICATION_JSON_VALUE)
  public Flux<Country> getAll() {
    return findCountry.findAll();
  }

  @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
  public Publisher<ResponseEntity<CountryResponse>> post(
      @Valid @RequestBody CountryRequest countryRequest,
      UriComponentsBuilder uriComponentsBuilder) {

    return saveCountry
        .createCountry(countryRequest.toCountry())
        .map(
            country -> {
              UriComponents uriComponents =
                  uriComponentsBuilder.path("/v1/country/{id}").buildAndExpand(country.id());
              var location = uriComponents.toUri();

              MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
              headers.put("Location", List.of(location.toString()));

              return new ResponseEntity<>(
                  new CountryResponse(country), headers, HttpStatus.CREATED);
            });
  }
}
