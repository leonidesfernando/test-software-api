package br.com.home.lab.softwaretesting.controller;

import br.com.home.lab.softwaretesting.controller.record.FormSearch;
import br.com.home.lab.softwaretesting.controller.record.LancamentoRecord;
import br.com.home.lab.softwaretesting.controller.record.ResultadoRecord;
import br.com.home.lab.softwaretesting.model.Categoria;
import br.com.home.lab.softwaretesting.model.Lancamento;
import br.com.home.lab.softwaretesting.model.ModelValidator;
import br.com.home.lab.softwaretesting.payload.MessageResponse;
import br.com.home.lab.softwaretesting.service.LancamentoService;
import br.com.home.lab.softwaretesting.util.Constantes;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolation;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/entries")
public class LancamentoController {

    @NonNull
    private LancamentoService lancamentoService;
    private final EnumSet<Categoria> categorias = EnumSet.allOf(Categoria.class);
    private int paginaCorrente;

    public LancamentoController(LancamentoService lancamentoService){
        this.lancamentoService = lancamentoService;
    }


    @PostMapping("/search")
    public ResponseEntity<ResultadoRecord> buscaAjax(@RequestBody FormSearch formSearch){
        ResultadoRecord resultado = lancamentoService.buscaAjax(formSearch);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addNew(@RequestBody LancamentoRecord newEntry){
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        lancamento = lancamentoService.salvar(lancamento);
        return ResponseEntity.ok().body(new MessageResponse("entry.added", lancamento.getId()));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<LancamentoRecord> getById(@PathVariable("id") Long id){
        var lancamento = lancamentoService.buscaPorId(id);
        DateFormat dateFormat = new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH);
        LancamentoRecord record = new LancamentoRecord(lancamento.getId(), lancamento.getDescricao(),
                lancamento.getValor().toString(),
                dateFormat.format(lancamento.getDataLancamento()),
                lancamento.getTipoLancamento().getTipo(),
                lancamento.getCategoria().getNome());
        return ResponseEntity.ok(record);
    }


    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody LancamentoRecord newEntry){
        //TODO: think about if will allow store a entry with a inexistent ID, today we accecpt, we ignore the ID and add a new one
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        var existingLancamento = lancamentoService.buscaPorId(lancamento.getId());
        if(existingLancamento.getId() != lancamento.getId()){
            throw new IllegalStateException("There are no entry with this id: " + lancamento.getId());
        }
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        lancamentoService.salvar(lancamento);
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

        lancamentoService.remover(id);
        return ResponseEntity.ok().body(new MessageResponse("entry.removed", id));
    }

    @DeleteMapping(value = "/removeAll")
    public ResponseEntity<MessageResponse> removeAll(){

        lancamentoService.truncateTable();
        return ResponseEntity.ok().body(new MessageResponse("all.entries.have.been.removed"));
    }
}
