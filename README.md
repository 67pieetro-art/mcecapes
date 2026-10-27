# MCE Capes (Forge 1.8.9)

Mod **somente client** que adiciona capas cosméticas **visíveis só para você**.
Nada é enviado ao servidor, então outros jogadores não veem a capa.

## Como usar no jogo
| Comando | O que faz |
|---|---|
| `/cape` | lista as capas |
| `/cape mce` | equipa a capa MCE |
| `/cape off` | remove a capa customizada |
| `/cape reload` | recarrega as imagens da pasta |
| `/cape folder` | mostra o caminho da pasta de capas |

Aliases: `/capa`, `/mcecape`. A escolha fica salva em `.minecraft/config/mcecapes/settings.properties`.
A capa **MCE é equipada por padrão**. Capas incluídas: `mce`, `aurora`, `crimson`, `midnight`.

## Textura oficial da capa MCE
A `mce.png` incluída é um **placeholder** (arte original). Para usar a textura real:
coloque o seu `mce.png` (64x32 ou HD 2:1, ex.: 128x64) em `.minecraft/config/mcecapes/` e use `/cape reload`.
Qualquer outro `.png` colocado nessa pasta vira uma capa nova (`/cape nome_do_arquivo`).

## Como gerar o .jar
Precisa do **JDK 8** (ForgeGradle 2.1 não funciona em Java mais novo).

    ./gradlew setupDecompWorkspace build      (Windows: gradlew.bat ...)

O jar sai em `build/libs/MCECapes-1.8.9-1.0.0.jar`. Coloque em `.minecraft/mods/` com o Forge 1.8.9 (11.15.1.2318).

### Sem instalar nada: GitHub Actions
Suba esta pasta para um repositório no GitHub. A aba **Actions** roda `.github/workflows/build.yml`
e disponibiliza o `.jar` como artefato para download.

### Alternativa: MDK oficial
Se o Gradle reclamar de dependências, baixe o MDK 1.8.9 do Forge e copie `src/` para dentro dele.
O código é compatível com o MDK padrão (mappings `stable_22`).
