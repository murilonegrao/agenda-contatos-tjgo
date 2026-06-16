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

/**
 * Interface de console (RNF05) — a entrega acadêmica. Menu de texto que dirige a
 * {@link AgendaService}. Toda exceção de domínio vira mensagem amigável; o programa
 * não quebra com id inexistente nem violação de regra (CA07, CA09).
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
                case "5" -> menuTelefones();
                case "6" -> menuEmails();
                case "7" -> buscar();
                case "0" -> sair = true;
                default  -> System.out.println("Opção inválida.");
            }
        }
        System.out.println("Até logo!");
    }

    private void menuPrincipal() {
        System.out.println("""

                ---------------- MENU ----------------
                 1) Unidades   (CRUD)
                 2) Contatos   (CRUD)
                 3) Lotação    (vincular/desvincular contato ↔ unidade)
                 4) Responsável de unidade (definir/trocar/remover)
                 5) Telefones  (adicionar/listar/remover)
                 6) E-mails    (adicionar/listar/remover)
                 7) Buscar por nome
                 0) Sair
                --------------------------------------""");
    }

    // ============================================================ Unidades

    private void menuUnidades() {
        System.out.println("""

                --- UNIDADES ---
                 1) Cadastrar   2) Listar   3) Editar   4) Remover   0) Voltar""");
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
        });
    }

    private void listarUnidades() {
        executar(() -> {
            List<Unidade> us = service.listarUnidades();
            if (us.isEmpty()) {
                System.out.println("(nenhuma unidade cadastrada)");
                return;
            }
            for (Unidade u : us) {
                String resp = u.getResponsavelId() == null ? "—"
                        : nomeContato(u.getResponsavelId());
                System.out.println(u + "  | responsável: " + resp);
            }
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
                 1) Cadastrar   2) Listar   3) Editar   4) Remover   0) Voltar""");
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
        });
    }

    private void listarContatos() {
        executar(() -> {
            List<Contato> cts = service.listarContatos();
            if (cts.isEmpty()) {
                System.out.println("(nenhum contato cadastrado)");
                return;
            }
            cts.forEach(System.out::println);
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

    // ============================================================ Telefones

    private void menuTelefones() {
        System.out.println("""

                --- TELEFONES ---
                 1) Adicionar a unidade   2) Adicionar a contato
                 3) Listar de unidade     4) Listar de contato
                 5) Remover por id        0) Voltar""");
        switch (lerLinha("Opção: ")) {
            case "1" -> executar(() -> {
                Telefone t = lerTelefone();
                t.setUnidadeId(lerInt("Id da unidade dona: "));
                System.out.println("✔ Telefone id=" + service.addTelefone(t) + " adicionado.");
            });
            case "2" -> executar(() -> {
                Telefone t = lerTelefone();
                t.setContatoId(lerInt("Id do contato dono: "));
                System.out.println("✔ Telefone id=" + service.addTelefone(t) + " adicionado.");
            });
            case "3" -> executar(() -> {
                List<Telefone> ts = service.telefonesDaUnidade(lerInt("Id da unidade: "));
                imprimirOuVazio(ts, "(sem telefones)");
            });
            case "4" -> executar(() -> {
                List<Telefone> ts = service.telefonesDoContato(lerInt("Id do contato: "));
                imprimirOuVazio(ts, "(sem telefones)");
            });
            case "5" -> executar(() -> {
                service.removerTelefone(lerInt("Id do telefone: "));
                System.out.println("✔ Telefone removido.");
            });
            case "0" -> { /* volta */ }
            default  -> System.out.println("Opção inválida.");
        }
    }

    // ============================================================ E-mails

    private void menuEmails() {
        System.out.println("""

                --- E-MAILS ---
                 1) Adicionar a unidade   2) Adicionar a contato
                 3) Listar de unidade     4) Listar de contato
                 5) Remover por id        0) Voltar""");
        switch (lerLinha("Opção: ")) {
            case "1" -> executar(() -> {
                Email e = lerEmail();
                e.setUnidadeId(lerInt("Id da unidade dona: "));
                System.out.println("✔ E-mail id=" + service.addEmail(e) + " adicionado.");
            });
            case "2" -> executar(() -> {
                Email e = lerEmail();
                e.setContatoId(lerInt("Id do contato dono: "));
                System.out.println("✔ E-mail id=" + service.addEmail(e) + " adicionado.");
            });
            case "3" -> executar(() -> {
                List<Email> es = service.emailsDaUnidade(lerInt("Id da unidade: "));
                imprimirOuVazio(es, "(sem e-mails)");
            });
            case "4" -> executar(() -> {
                List<Email> es = service.emailsDoContato(lerInt("Id do contato: "));
                imprimirOuVazio(es, "(sem e-mails)");
            });
            case "5" -> executar(() -> {
                service.removerEmail(lerInt("Id do e-mail: "));
                System.out.println("✔ E-mail removido.");
            });
            case "0" -> { /* volta */ }
            default  -> System.out.println("Opção inválida.");
        }
    }

    // ============================================================ Busca

    private void buscar() {
        executar(() -> {
            String termo = lerLinha("Termo (nome de unidade ou contato): ");
            List<ResultadoBusca> r = service.buscarPorNome(termo);
            if (r.isEmpty()) {
                System.out.println("(nada encontrado para \"" + termo + "\")");
                return;
            }
            System.out.println(r.size() + " resultado(s):");
            r.forEach(System.out::println);
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

    private void imprimirOuVazio(List<?> lista, String vazio) {
        if (lista.isEmpty()) System.out.println(vazio);
        else lista.forEach(System.out::println);
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
