package br.jus.tjgo.agenda.app;

import br.jus.tjgo.agenda.model.Contato;
import br.jus.tjgo.agenda.model.Email;
import br.jus.tjgo.agenda.model.ResultadoBusca;
import br.jus.tjgo.agenda.model.Telefone;
import br.jus.tjgo.agenda.model.TipoTelefone;
import br.jus.tjgo.agenda.model.TipoUnidade;
import br.jus.tjgo.agenda.model.Unidade;
import br.jus.tjgo.agenda.service.AgendaException;
import br.jus.tjgo.agenda.service.AgendaService;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Interface de console (RNF05) — menu de texto que dirige a {@link AgendaService}.
 * Toda exceção de domínio vira mensagem amigável; o programa não quebra com id
 * inexistente nem violação de regra (CA07, CA09).
 */
public class MenuConsole {

    private final AgendaService service = new AgendaService();
    private final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        new MenuConsole().rodar();
    }

    private void rodar() {
        System.out.println("==========================================");
        System.out.println("  Agenda de Contatos TJGO — Console v0.1");
        System.out.println("==========================================");
        boolean sair = false;
        while (!sair) {
            menuPrincipal();
            switch (lerLinha("Opção: ")) {
                case "1" -> menuUnidades();
                case "2" -> menuContatos();
                case "3" -> menuLotacao();
                case "4" -> menuResponsavel();
                case "5" -> buscar();
                case "0" -> sair = true;
                default  -> System.out.println("Opção inválida.");
            }
        }
        System.out.println("Até logo!");
    }

    private void menuPrincipal() {
        System.out.println("""

                ---------------- MENU ----------------
                 1) Unidades   (CRUD + telefones/e-mails)
                 2) Contatos   (CRUD + telefones/e-mails)
                 3) Lotação    (vincular/desvincular contato ↔ unidade)
                 4) Responsável de unidade (definir/trocar/remover)
                 5) Buscar por nome
                 0) Sair
                --------------------------------------""");
    }

    // ============================================================ Unidades

    private void menuUnidades() {
        System.out.println("""

                --- UNIDADES ---
                 1) Cadastrar   2) Listar   3) Editar (+ telefones/e-mails)   4) Remover   0) Voltar""");
        switch (lerLinha("Opção: ")) {
            case "1" -> cadastrarUnidade();
            case "2" -> listarUnidades();
            case "3" -> editarUnidade();
            case "4" -> removerUnidade();
            case "0" -> { /* volta */ }
            default  -> System.out.println("Opção inválida.");
        }
    }

    private void cadastrarUnidade() {
        executar(() -> {
            Unidade u = new Unidade();
            u.setNome(lerLinha("Nome: "));
            u.setTipo(lerTipoUnidade());
            u.setEndereco(lerLinha("Endereço (opcional): "));
            int id = service.salvarUnidade(u);
            System.out.println("✔ Unidade cadastrada com id=" + id);
            exigirTelefonesEmails(id, null);
        });
    }

    private void listarUnidades() {
        executar(() -> {
            List<Unidade> us = service.listarUnidades();
            if (us.isEmpty()) {
                System.out.println("(nenhuma unidade cadastrada)");
                return;
            }
            us.forEach(this::imprimirUnidade);
        });
    }

    private void editarUnidade() {
        executar(() -> {
            Unidade u = service.buscarUnidade(lerInt("Id da unidade: "));
            System.out.println("Editando: " + u);
            u.setNome(lerLinhaPadrao("Nome", u.getNome()));
            System.out.println("Tipo atual: " + u.getTipo());
            u.setTipo(lerTipoUnidade());
            u.setEndereco(lerLinhaPadrao("Endereço", u.getEndereco()));
            service.salvarUnidade(u);
            System.out.println("✔ Unidade atualizada.");
            gerenciarTelefonesEmails(u.getId(), null);
        });
    }

    private void removerUnidade() {
        executar(() -> {
            int id = lerInt("Id da unidade a remover: ");
            service.removerUnidade(id);
            System.out.println("✔ Unidade removida (lotações e telefones/e-mails dela também).");
        });
    }

    // ============================================================ Contatos

    private void menuContatos() {
        System.out.println("""

                --- CONTATOS ---
                 1) Cadastrar   2) Listar   3) Editar (+ telefones/e-mails)   4) Remover   0) Voltar""");
        switch (lerLinha("Opção: ")) {
            case "1" -> cadastrarContato();
            case "2" -> listarContatos();
            case "3" -> editarContato();
            case "4" -> removerContato();
            case "0" -> { /* volta */ }
            default  -> System.out.println("Opção inválida.");
        }
    }

    private void cadastrarContato() {
        executar(() -> {
            Contato ct = new Contato();
            ct.setNome(lerLinha("Nome: "));
            ct.setCargo(lerLinha("Cargo (opcional): "));
            int id = service.salvarContato(ct);
            System.out.println("✔ Contato cadastrado com id=" + id);
            exigirTelefonesEmails(null, id);
        });
    }

    private void listarContatos() {
        executar(() -> {
            List<Contato> cts = service.listarContatos();
            if (cts.isEmpty()) {
                System.out.println("(nenhum contato cadastrado)");
                return;
            }
            cts.forEach(this::imprimirContato);
        });
    }

    private void editarContato() {
        executar(() -> {
            Contato ct = service.buscarContato(lerInt("Id do contato: "));
            System.out.println("Editando: " + ct);
            ct.setNome(lerLinhaPadrao("Nome", ct.getNome()));
            ct.setCargo(lerLinhaPadrao("Cargo", ct.getCargo()));
            service.salvarContato(ct);
            System.out.println("✔ Contato atualizado.");
            gerenciarTelefonesEmails(null, ct.getId());
        });
    }

    private void removerContato() {
        executar(() -> {
            int id = lerInt("Id do contato a remover: ");
            service.removerContato(id);
            System.out.println("✔ Contato removido (vira NULL onde era responsável).");
        });
    }

    // ============================================================ Lotação

    private void menuLotacao() {
        System.out.println("""

                --- LOTAÇÃO (N:N) ---
                 1) Vincular contato a unidade
                 2) Desvincular contato de unidade
                 3) Listar contatos de uma unidade
                 4) Listar unidades de um contato
                 0) Voltar""");
        switch (lerLinha("Opção: ")) {
            case "1" -> executar(() -> {
                int u = lerInt("Id da unidade: ");
                int c = lerInt("Id do contato: ");
                service.vincularLotacao(u, c);
                System.out.println("✔ Contato vinculado à unidade.");
            });
            case "2" -> executar(() -> {
                int u = lerInt("Id da unidade: ");
                int c = lerInt("Id do contato: ");
                service.desvincularLotacao(u, c);
                System.out.println("✔ Vínculo removido.");
            });
            case "3" -> executar(() -> {
                List<Contato> cts = service.contatosDaUnidade(lerInt("Id da unidade: "));
                if (cts.isEmpty()) System.out.println("(unidade sem contatos lotados)");
                else cts.forEach(System.out::println);
            });
            case "4" -> executar(() -> {
                List<Unidade> us = service.unidadesDoContato(lerInt("Id do contato: "));
                if (us.isEmpty()) System.out.println("(contato sem lotações)");
                else us.forEach(System.out::println);
            });
            case "0" -> { /* volta */ }
            default  -> System.out.println("Opção inválida.");
        }
    }

    // ============================================================ Responsável

    private void menuResponsavel() {
        System.out.println("""

                --- RESPONSÁVEL DA UNIDADE ---
                 1) Definir / trocar responsável
                 2) Remover responsável
                 0) Voltar""");
        switch (lerLinha("Opção: ")) {
            case "1" -> executar(() -> {
                int u = lerInt("Id da unidade: ");
                int c = lerInt("Id do contato responsável: ");
                service.definirResponsavel(u, c);
                System.out.println("✔ Responsável definido.");
            });
            case "2" -> executar(() -> {
                int u = lerInt("Id da unidade: ");
                service.definirResponsavel(u, null);
                System.out.println("✔ Responsável removido (unidade sem responsável).");
            });
            case "0" -> { /* volta */ }
            default  -> System.out.println("Opção inválida.");
        }
    }

    // =================================================== Telefones / E-mails (atributos)

    /**
     * Telefones e e-mails são atributos da unidade/contato — por isso são geridos aqui, dentro do
     * Editar, e não num menu próprio. Mantém RN09: não deixa remover o último telefone nem o último
     * e-mail. Exatamente um de {@code unidadeId}/{@code contatoId} vem preenchido (o dono).
     */
    private void gerenciarTelefonesEmails(Integer unidadeId, Integer contatoId) {
        boolean ehUnidade = unidadeId != null;
        boolean voltar = false;
        while (!voltar) {
            List<Telefone> tels = ehUnidade
                    ? service.telefonesDaUnidade(unidadeId) : service.telefonesDoContato(contatoId);
            List<Email> emails = ehUnidade
                    ? service.emailsDaUnidade(unidadeId) : service.emailsDoContato(contatoId);
            System.out.println("\n--- TELEFONES / E-MAILS ---");
            System.out.println("  telefones: " + juntar(tels));
            System.out.println("  e-mails:   " + juntar(emails));
            System.out.println(" 1) Adicionar telefone   2) Remover telefone");
            System.out.println(" 3) Adicionar e-mail     4) Remover e-mail");
            System.out.println(" 0) Voltar");
            switch (lerLinha("Opção: ")) {
                case "1" -> tentarAddTelefone(unidadeId, contatoId);
                case "2" -> removerTelefone(tels);
                case "3" -> tentarAddEmail(unidadeId, contatoId);
                case "4" -> removerEmail(emails);
                case "0" -> voltar = true;
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    /** Remove um telefone do cadastro atual, mantendo RN09 (não remove o último). */
    private void removerTelefone(List<Telefone> atuais) {
        if (atuais.size() <= 1) {
            System.out.println("⚠ Não dá pra remover: é o único telefone e todo cadastro precisa de ao menos um (RN09).");
            return;
        }
        executar(() -> {
            int id = lerInt("Id do telefone a remover: ");
            if (atuais.stream().noneMatch(t -> t.getId() == id)) {
                System.out.println("⚠ Esse telefone não é deste cadastro. Veja os ids na lista acima.");
                return;
            }
            service.removerTelefone(id);
            System.out.println("✔ Telefone removido.");
        });
    }

    /** Remove um e-mail do cadastro atual, mantendo RN09 (não remove o último). */
    private void removerEmail(List<Email> atuais) {
        if (atuais.size() <= 1) {
            System.out.println("⚠ Não dá pra remover: é o único e-mail e todo cadastro precisa de ao menos um (RN09).");
            return;
        }
        executar(() -> {
            int id = lerInt("Id do e-mail a remover: ");
            if (atuais.stream().noneMatch(e -> e.getId() == id)) {
                System.out.println("⚠ Esse e-mail não é deste cadastro. Veja os ids na lista acima.");
                return;
            }
            service.removerEmail(id);
            System.out.println("✔ E-mail removido.");
        });
    }

    // ============================================================ Busca

    private void buscar() {
        executar(() -> {
            String termo = lerLinha("Termo (nome de unidade ou contato): ");
            List<ResultadoBusca> achados = service.buscarPorNome(termo);
            if (achados.isEmpty()) {
                System.out.println("(nada encontrado para \"" + termo + "\")");
                return;
            }
            System.out.println(achados.size() + " resultado(s):");
            for (ResultadoBusca r : achados) {
                if ("UNIDADE".equals(r.origem())) {
                    System.out.println("• UNIDADE");
                    imprimirUnidade(service.buscarUnidade(r.id()));
                } else {
                    System.out.println("• CONTATO");
                    imprimirContato(service.buscarContato(r.id()));
                }
            }
        });
    }

    // ============================================================ Helpers I/O

    /** Executa um bloco capturando AgendaException → mensagem amigável (CA07/CA09). */
    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (AgendaException e) {
            System.out.println("⚠ " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("⚠ Erro inesperado: " + e.getMessage());
        }
    }

    /** Lista a unidade já com responsável, telefones, e-mails e contatos lotados — consulta sem precisar do id. */
    private void imprimirUnidade(Unidade u) {
        String resp = u.getResponsavelId() == null ? "—" : nomeContato(u.getResponsavelId());
        System.out.println(u);
        System.out.println("    responsável: " + resp);
        System.out.println("    telefones:   " + juntar(service.telefonesDaUnidade(u.getId())));
        System.out.println("    e-mails:     " + juntar(service.emailsDaUnidade(u.getId())));
        System.out.println("    contatos:    " + juntar(service.contatosDaUnidade(u.getId())));
    }

    /** Lista o contato já com telefones, e-mails e as unidades onde é lotado — consulta sem precisar do id. */
    private void imprimirContato(Contato ct) {
        System.out.println(ct);
        System.out.println("    telefones: " + juntar(service.telefonesDoContato(ct.getId())));
        System.out.println("    e-mails:   " + juntar(service.emailsDoContato(ct.getId())));
        System.out.println("    lotado em: " + juntar(service.unidadesDoContato(ct.getId())));
    }

    /** Junta a lista por vírgula para exibição inline; "(nenhum)" quando vazia. */
    private String juntar(List<?> itens) {
        return itens.isEmpty() ? "(nenhum)"
                : itens.stream().map(Object::toString).collect(Collectors.joining(", "));
    }

    /**
     * Logo após criar uma unidade/contato, exige cadastrar pelo menos um telefone E um e-mail
     * (RN09 — cadastro contactável), permitindo adicionar quantos mais quiser.
     * Exatamente um dos ids vem preenchido (o dono); o outro é {@code null} — respeita o arco exclusivo.
     */
    private void exigirTelefonesEmails(Integer unidadeId, Integer contatoId) {
        System.out.println("Cadastre ao menos 1 telefone e 1 e-mail (obrigatório).");
        int tels = 0;
        while (tels == 0 || confirmar("Adicionar outro telefone? (s/N): ")) {
            if (tentarAddTelefone(unidadeId, contatoId)) tels++;
            else if (tels == 0) System.out.println("  Pelo menos um telefone é obrigatório.");
        }
        int emails = 0;
        while (emails == 0 || confirmar("Adicionar outro e-mail? (s/N): ")) {
            if (tentarAddEmail(unidadeId, contatoId)) emails++;
            else if (emails == 0) System.out.println("  Pelo menos um e-mail é obrigatório.");
        }
    }

    /** Lê e adiciona um telefone do dono indicado; devolve false (com aviso) se a entrada for rejeitada. */
    private boolean tentarAddTelefone(Integer unidadeId, Integer contatoId) {
        try {
            Telefone t = lerTelefone();
            t.setUnidadeId(unidadeId);
            t.setContatoId(contatoId);
            System.out.println("✔ Telefone id=" + service.addTelefone(t) + " adicionado.");
            return true;
        } catch (RuntimeException ex) {
            System.out.println("⚠ " + ex.getMessage());
            return false;
        }
    }

    /** Lê e adiciona um e-mail do dono indicado; devolve false (com aviso) se a entrada for rejeitada. */
    private boolean tentarAddEmail(Integer unidadeId, Integer contatoId) {
        try {
            Email e = lerEmail();
            e.setUnidadeId(unidadeId);
            e.setContatoId(contatoId);
            System.out.println("✔ E-mail id=" + service.addEmail(e) + " adicionado.");
            return true;
        } catch (RuntimeException ex) {
            System.out.println("⚠ " + ex.getMessage());
            return false;
        }
    }

    /** Lê uma confirmação sim/não; só "s"/"sim" (qualquer caixa) confirma. */
    private boolean confirmar(String prompt) {
        String s = lerLinha(prompt).toLowerCase();
        return s.equals("s") || s.equals("sim");
    }

    private String nomeContato(int id) {
        try {
            return service.buscarContato(id).getNome();
        } catch (AgendaException e) {
            return "id=" + id;
        }
    }

    private Telefone lerTelefone() {
        Telefone t = new Telefone();
        t.setNumero(lerNumeroTelefone());
        t.setTipo(lerTipoTelefone());
        return t;
    }

    private Email lerEmail() {
        Email e = new Email();
        e.setEndereco(lerLinha("Endereço de e-mail: "));
        String tipo = lerLinha("Tipo (opcional, ex.: institucional): ");
        e.setTipo(tipo.isBlank() ? null : tipo);
        return e;
    }

    private String lerLinha(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    /** Lê com valor padrão: Enter mantém o atual (usado nas edições). */
    private String lerLinhaPadrao(String campo, String atual) {
        System.out.print(campo + " [" + (atual == null ? "" : atual) + "]: ");
        String v = sc.nextLine().trim();
        return v.isBlank() ? atual : v;
    }

    private int lerInt(String prompt) {
        while (true) {
            String s = lerLinha(prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    /** Aceita número com máscara e devolve só dígitos; sugere DDI 55 quando faltar. */
    private String lerNumeroTelefone() {
        String bruto = lerLinha("Número (com DDI; pode digitar com máscara): ");
        String digitos = bruto.replaceAll("\\D", "");
        // DDI 55 default (RN08): se parecer número nacional sem DDI, prefixa 55.
        if ((digitos.length() == 10 || digitos.length() == 11) && !digitos.startsWith("55")) {
            digitos = "55" + digitos;
            System.out.println("  (assumindo DDI 55 → " + digitos + ")");
        }
        return digitos;
    }

    private TipoUnidade lerTipoUnidade() {
        while (true) {
            String s = lerLinha("Tipo [UNIDADE_PRISIONAL | ORGAO | OUTRO]: ").toUpperCase();
            try {
                return TipoUnidade.valueOf(s);
            } catch (IllegalArgumentException e) {
                System.out.println("Tipo inválido. Use um dos valores entre colchetes.");
            }
        }
    }

    private TipoTelefone lerTipoTelefone() {
        while (true) {
            String s = lerLinha("Tipo [CELULAR | FIXO | WHATSAPP | PLANTAO]: ").toUpperCase();
            try {
                return TipoTelefone.valueOf(s);
            } catch (IllegalArgumentException e) {
                System.out.println("Tipo inválido. Use um dos valores entre colchetes.");
            }
        }
    }
}
