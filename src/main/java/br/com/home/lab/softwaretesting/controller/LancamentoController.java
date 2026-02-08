package br.com.home.lab.softwaretesting.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/entries")
public class LancamentoController {
/*
    @NonNull
    private LancamentoService lancamentoService;
    private final EnumSet<Categoria> categorias = EnumSet.allOf(Categoria.class);
    private int paginaCorrente;

    public LancamentoController(LancamentoService lancamentoService){
        this.lancamentoService = lancamentoService;
    }


    @PostMapping("/search")
    public ResponseEntity<ResultadoRecord> buscaAjax(@RequestBody BuscaForm buscaForm){
        ResultadoRecord resultado = lancamentoService.buscaAjax(buscaForm);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/add")
    public ResponseEntity<MessageResponse> addNew(@RequestBody LancamentoRecord newEntry){
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        lancamentoService.salvar(lancamento);
        return ResponseEntity.ok().body(new MessageResponse("entry.added"));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<LancamentoRecord> getById(@PathVariable("id") Long id){
        var lancamento = lancamentoService.buscaPorId(id);
        DateFormat dateFormat = new SimpleDateFormat(Constantes.dd_MM_yyyy_SLASH);
        LancamentoRecord record = new LancamentoRecord(lancamento.getId(), lancamento.getDescricao(),
                lancamento.getValor().toString(),
                dateFormat.format(lancamento.getDataLancamento()),
                lancamento.getTipoLancamento().getTipo(),
                lancamento.getCategory().getNome());
        return ResponseEntity.ok(record);
    }


    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody LancamentoRecord newEntry){
        //TODO: think about if will allow store a entry with a inexistent ID, today we accecpt, we ignore the ID and add a new one
        Lancamento lancamento = newEntry.build();
        String violations = validate(lancamento);
        if(!violations.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse(violations));
        }
        lancamentoService.salvar(lancamento);
        return ResponseEntity.ok().body(new MessageResponse("entry.updated"));
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
        return ResponseEntity.ok().body(new MessageResponse("entry.removed"));
    }

    @DeleteMapping(value = "/removeAll")
    public ResponseEntity<MessageResponse> removeAll(){

        lancamentoService.truncateTable();
        return ResponseEntity.ok().body(new MessageResponse("all.entries.have.been.removed"));
    } */
}
