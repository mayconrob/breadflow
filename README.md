## 🌾 Sobre o BreadFlow

O **BreadFlow** é um aplicativo Android nativo, gratuito e de código aberto (*open source*), criado para ajudar você a administrar suas finanças diárias com consciência, serenidade e responsabilidade. 

Inspirado no conceito do **"pão de cada dia"**, o BreadFlow reflete a gratidão pelo sustento que nos alcança a cada manhã. A espiga de trigo representa a provisão divina e o fruto do trabalho honesto; o fluxo harmônico (*flow*) simboliza o cuidado contínuo e a boa mordomia com aquilo que recebemos e gastamos.

---

## 🛡️ Nossos Pilares Inegociáveis

1. 🌐 **Sempre Open Source (GPLv3)**: Software livre por convicção. O código é transparente, auditável e continuará acessível a todos, sem paywalls, planos pagos ou recursos ocultos.
2. 🔒 **Device-First (Privacidade Absoluta)**: Os dados financeiros pertencem **única e exclusivamente a você**. O armazenamento é 100% local no seu smartphone (Room Database). Nenhum extrato, saldo ou transação é enviado para nuvens de terceiros ou minerado para anúncios.
3. 🌾 **Foco & Fluidez**: Interface rápida e intuitiva (*Hero Amount*, digitação estilo bancário em tempo real e divulgação progressiva). Registrar um café ou o almoço leva menos de 3 segundos, sem formulários burocráticos.
4. 🏗️ **Qualidade Técnica & Estudo Contínuo**: Desenvolvido na vanguarda do ecossistema Android — Single Activity Architecture, Jetpack Compose, Material 3, Clean Architecture (Domain, Data, UI), Coroutines, Flow e Hilt.
5. 🤖 **Parceria com Inteligência Artificial**: Desenvolvido explorando o estado da arte da engenharia de software contemporânea, onde o desenvolvedor e a IA colaboram para projetar componentes visuais e padrões arquiteturais de alta qualidade.

---

## ✨ Funcionalidades

- 💰 **Digitação Estilo Caixa Eletrônico:** Entrada do valor ágil da direita para a esquerda em formato de centavos e milhares (`pt-BR`).
- 📊 **Gestão Completa de Transações:** Cadastro rápido de entradas e saídas, com descrição opcional e suporte a tags e observações em acordeão expansível.
- 🗂️ **Categorização Inteligente:** Organização de receitas e despesas por categorias personalizáveis com cores e limites de orçamento.
- 📈 **Painel de Controle (Dashboard):** Visão consolidada do saldo total, entradas/saídas do mês e acompanhamento do teto de orçamento por categoria.
- 📜 **Extrato Financeiro Avançado:** Histórico ordenável (mais recentes, mais antigas, maior/menor valor) com filtros cumulativos por data, tipo e categorias.
- 💾 **100% Offline e Seguro:** Opera perfeitamente sem internet e guarda tudo localmente.
- 🎨 **Material 3 Dark Theme:** Visual escuro elegante, moderno e confortável para o uso diário.

---

## 🌐 Presença Oficial & Comunidade

Acompanhe o projeto, novidades e atualizações nas redes oficiais pelo identificador **`@useBreadFlow`**:

- 🌍 **Website Oficial:** [breadflow.com.br](https://breadflow.com.br)
- 📲 **Redes Sociais (`@useBreadFlow`):**

---

## 🛠️ Tecnologias e Arquitetura

O projeto foi estruturado seguindo rigorosamente as recomendações de arquitetura moderna da Google:

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) com [Material 3](https://m3.material.io/)
- **Arquitetura:** Clean Architecture + MVVM (Model-View-ViewModel) com separação estrita de camadas (`common`, `data`, `domain`, 
- `ui`)
- **Persistência Local:** [Room Database](https://developer.android.com/training/data-storage/room)
- **Concorrência & Reatividade:** Kotlin Coroutines & StateFlow
- **Injeção de Dependências:** Hilt (Dagger)
- **Gerenciador de Build:** Gradle com Kotlin DSL (`build.gradle.kts`)

---

## 🚀 Melhoria Contínua & Roadmap

O **BreadFlow** evolui constantemente. Nossos próximos passos planejados incluem:

- [ ] **Testes Automatizados:** Implementação de testes unitários para ViewModels e Casos de Uso, além de testes de interface com Compose.
- [ ] **Exportação & Backup Local:** Exportação/importação de relatórios em CSV e backup local seguro em JSON.
- [ ] **Gráficos e Estatísticas:** Visualização analítica em gráficos de barra e pizza por categoria ao longo dos meses.
- [ ] **Internacionalização (i18n):** Suporte nativo para múltiplos idiomas (Inglês e Espanhol).
- [ ] **Segurança Adicional:**
  - Desbloqueio biométrico (`BiometricPrompt`) para abertura do aplicativo.
  - Criptografia transparente do banco de dados Room via `SQLCipher`.

---

## 📄 Licença

Este projeto é um software livre distribuído sob os termos da licença **GNU General Public License v3.0 (GPLv3)**. Você tem a liberdade de estudar, modificar e distribuir o código de forma aberta e transparente. Para mais detalhes, consulte o arquivo [LICENSE](LICENSE).

---

Desenvolvido com 💙 e propósito por **[Maycon Roberto](https://github.com/mayconrob)**
