package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.controller.record.DataEncrypt;
import br.com.home.lab.softwaretesting.controller.record.EntryRecord;
import br.com.home.lab.softwaretesting.controller.record.FormSearch;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import br.com.home.lab.softwaretesting.controller.record.UserIDRecord;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.ModelValidator;
import br.com.home.lab.softwaretesting.payload.MessageResponse;
import br.com.home.lab.softwaretesting.repository.UserRepository;
import br.com.home.lab.softwaretesting.service.EntryService;
import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/entries")
public class EntryController {

    @NonNull
    private final EntryService entryService;

    @NonNull
    private final UserRepository userRepository;

    public EntryController(EntryService entryService, UserRepository userRepository){
        this.entryService = entryService;
        this.userRepository = userRepository;
    }


    @PostMapping("/search2")
    public ResponseEntity<DataEncrypt> ajaxSearchEncrypt(@RequestBody FormSearch formSearch){
        ResultRecord result = entryService.ajaxSearch(formSearch);
        DataEncrypt data = new DataEncrypt(result.encrypt());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/search")
    public ResponseEntity<ResultRecord> ajaxSearch(@RequestBody FormSearch formSearch){
        ResultRecord result = entryService.ajaxSearch(formSearch);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addNew(@RequestBody EntryRecord newEntry){
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        var responseEntity = AuthController.isLoggedUserForbidden(lancamento.getUser().getId());
        if(responseEntity != null){
            return responseEntity;
        }
        lancamento.setUser(userRepository.getById(lancamento.getUser().getId()));
        lancamento = entryService.save(lancamento);
        return ResponseEntity.ok().body(new MessageResponse("entry.added", lancamento.getId()));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id){
        var lancamento = entryService.searchById(id);
        var responseEntity = AuthController.isLoggedUserForbidden(lancamento.getUser().getId());
        if(responseEntity != null){
            return responseEntity;
        }
        DateFormat dateFormat = new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH);
        EntryRecord entryRecord = new EntryRecord(lancamento.getId(), lancamento.getDescricao(),
                lancamento.getValor().toString(),
                dateFormat.format(lancamento.getDataLancamento()),
                lancamento.getTipoLancamento().getTipo(),
                lancamento.getCategory().getNome(),
                lancamento.getUser().getId());
        return ResponseEntity.ok(entryRecord);
    }

    @PutMapping("/update")
    public ResponseEntity<MessageResponse> update(@RequestBody EntryRecord newEntry){
        //TODO: think about if will allow store a entry with a inexistent ID, today we accecpt, we ignore the ID and add a new one
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        var existingEntry = entryService.searchById(lancamento.getId());
        if(existingEntry.getId() != lancamento.getId()){
            throw new IllegalStateException("There are no entry with this id: " + lancamento.getId());
        }
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        var responseEntity = AuthController.isLoggedUserForbidden(lancamento.getUser().getId());
        if(responseEntity != null){
            return responseEntity;
        }
        entryService.save(lancamento);
        return ResponseEntity.ok().body(new MessageResponse("entry.updated", lancamento.getId()));
    }

    private String validate(Lancamento lancamento){
        Set<ConstraintViolation<Lancamento>> violations = ModelValidator.getInstance().validate(lancamento);
        return violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(","));
    }

    @DeleteMapping(value = "/remove/{id}")
    public ResponseEntity<MessageResponse> remove(@PathVariable("id") Long id){
        var lancamento = entryService.searchById(id);
        var responseEntity = AuthController.isLoggedUserForbidden(lancamento.getUser().getId());
        if(responseEntity != null){
            return responseEntity;
        }
        entryService.remover(id);
        return ResponseEntity.ok().body(new MessageResponse("entry.removed", id));
    }

    @DeleteMapping(value = "/removeAll")
    public ResponseEntity<MessageResponse> removeAll(@RequestBody UserIDRecord user){
        var responseEntity = AuthController.isLoggedUserForbidden(user.id());
        if(responseEntity != null){
            return responseEntity;
        }
        entryService.removeAllByUser(user.id());
        return ResponseEntity.ok().body(new MessageResponse("all.entries.have.been.removed"));
    }
}
