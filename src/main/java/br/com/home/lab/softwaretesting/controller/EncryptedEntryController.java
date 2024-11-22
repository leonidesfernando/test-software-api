package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.controller.record.DataEncrypt;
import br.com.home.lab.softwaretesting.controller.record.EntryRecord;
import br.com.home.lab.softwaretesting.controller.record.FormSearch;
import br.com.home.lab.softwaretesting.controller.record.ResultRecord;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.ModelValidator;
import br.com.home.lab.softwaretesting.payload.MessageResponse;
import br.com.home.lab.softwaretesting.security.EncryptUtil;
import br.com.home.lab.softwaretesting.service.EntryService;
import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/sec/entries")
public class EncryptedEntryController {

    @NonNull
    private final EntryService entryService;

    public EncryptedEntryController(EntryService entryService){
        this.entryService = entryService;
    }

    private DataEncrypt encode(Object data){
        EncryptUtil encryptUtil = new EncryptUtil();
        return new DataEncrypt(encryptUtil.encode(data));
    }

    private EntryRecord decode(String data){
        EncryptUtil encryptUtil = new EncryptUtil();
        return encryptUtil.decode(data, EntryRecord.class);
    }

    @PostMapping("/search")
    public ResponseEntity<DataEncrypt> ajaxSearchEncrypt(@RequestBody FormSearch formSearch){
        ResultRecord result = entryService.ajaxSearch(formSearch);
        DataEncrypt data = new DataEncrypt(result.encrypt());
        return ResponseEntity.ok(data);
    }


    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addNew(@RequestBody DataEncrypt data){
        EntryRecord newEntry = decode(data.data());
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        lancamento = entryService.save(lancamento);
        return ResponseEntity.ok().body(new MessageResponse("entry.added", lancamento.getId()));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<DataEncrypt> getById(@PathVariable("id") Long id){
        var lancamento = entryService.searchById(id);
        DateFormat dateFormat = new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH);
        EntryRecord entityRecord = new EntryRecord(lancamento.getId(), lancamento.getDescricao(),
                lancamento.getValor().toString(),
                dateFormat.format(lancamento.getDataLancamento()),
                lancamento.getTipoLancamento().getTipo(),
                lancamento.getCategory().getNome());
        return ResponseEntity.ok(encode(entityRecord));
    }


    @PutMapping("/update")
    public ResponseEntity<MessageResponse> update(@RequestBody DataEncrypt data){
        //TODO: think about if will allow store a entry with a inexistent ID, today we accecpt, we ignore the ID and add a new one
        EntryRecord newEntry = decode(data.data());
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        var existingEntry = entryService.searchById(lancamento.getId());
        if(existingEntry.getId() != lancamento.getId()){
            throw new IllegalStateException("There are no entry with this id: " + lancamento.getId());
        }
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
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
        entryService.remover(id);
        return ResponseEntity.ok().body(new MessageResponse("entry.removed", id));
    }

    @DeleteMapping(value = "/removeAll")
    public ResponseEntity<MessageResponse> removeAll(){
        entryService.truncateTable();
        return ResponseEntity.ok().body(new MessageResponse("all.entries.have.been.removed"));
    }
}
