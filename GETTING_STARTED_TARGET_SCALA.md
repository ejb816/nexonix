# Getting Started — Target: Scala

**Status: realized.** Scala is the only target with a working projection today. The whole
corpus is projection-canonical against it and three gates hold it that way.

One of a family — see also `GETTING_STARTED_TARGET_HASKELL.md` and
`GETTING_STARTED_TARGET_TYPESCRIPT.md`. All three follow the same structure, because the
part that matters is the same in each: **only sections 1, 4, 5 and 6 are genuinely
target-specific.**

> **The one thing that matters most: use Java 17.** Draco's Scala target is pinned to
> JDK 17. A newer JDK (21, 23, 25, …) fails with errors like `bad constant pool index`.

---

## 0. What is the same for every target

Authoring is target-independent. You write a **definition**; a **target** projects it.

```text
  author X.json          the definition — normative, the only form loaded at runtime
     |
     +-- X.drake         the surface — the same content, written for humans
     |
     +-- project ------> X.<target>   source in a programming language
     |
  register X in its domain's elementTypeNames
     |
  verify                 the gates hold definition, surface and projection together
```

Nothing in that loop is Scala's. What each target supplies is a **toolchain** (§1), a
**projection command** (§4), a way to **run** the result (§5), and its own
**command set** (§6). The rest of this file is those four things for Scala.

The definition format itself is described in `README.md`; the surface language is
specified in `src/main/resources/draco/drake.dlt`.

---

## 1. Toolchain

You need four things: **Git**, **JDK 17**, **sbt**, **scala-cli**. The Scala trio is
easiest in one shot with [Coursier](https://get-coursier.io) (`cs`); Git installs
separately.

### macOS

Using [Homebrew](https://brew.sh):

```bash
brew install git
brew install coursier/formulas/coursier
cs setup --jvm temurin:17 --yes          # sbt, scala-cli, scala + a Java 17
```

Open a **new** terminal, then pin Java 17 (zsh):

```bash
echo 'export JAVA_HOME="$(/usr/libexec/java_home -v 17)"' >> ~/.zshrc
source ~/.zshrc
```

### Linux / Unix

```bash
sudo apt-get update && sudo apt-get install -y git      # or your distro's manager

curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > cs
chmod +x cs
./cs setup --jvm temurin:17 --yes
```

Coursier adds itself and Java 17 to your shell profile — open a **new** terminal. For
ARM and other architectures see
[get-coursier.io](https://get-coursier.io/docs/cli-installation).

### Windows

**A — winget (PowerShell):**

```powershell
winget install --id Git.Git -e
winget install --id EclipseAdoptium.Temurin.17.JDK -e
winget install --id VirtusLab.ScalaCLI -e
winget install --id sbt.sbt -e            # if unavailable, use path B
```

**B — Coursier:** download `cs-x86_64-pc-win32.zip` from the
[releases](https://github.com/coursier/launchers/releases), unzip, then:

```powershell
.\cs.exe setup --jvm temurin:17 --yes
```

> The `bin/` tools are Bash scripts — run them from **Git Bash** or **WSL**. `sbt` and
> `scala-cli` work fine in PowerShell.

### Verify

Fresh terminal. Java must report **17**:

```bash
git --version
java -version        # -> openjdk version "17.x.x"   <- must be 17
sbt --version        # -> sbt ... 1.12.9
scala-cli version
```

---

## 2. Clone

```bash
git clone https://github.com/ejb816/nexonix.git
cd nexonix
```

---

## 3. Build and test

The first `sbt` command downloads sbt, Scala 2.13, and dependencies — a few minutes
**once**, then cached.

```bash
sbt compile
sbt test                                                # the whole suite
sbt "testOnly draco.DracoGenTest"                       # projection gate
sbt "testOnly draco.DrakeGenTest draco.DrakeParseTest"  # surface gates
sbt "testOnly draco.primes.PrimesRulesTest -- -z PrimesFromNaturalSequence"
```

Or the interactive shell, to avoid repeated JVM startup:

```bash
sbt
> test
> ~compile          # recompile on every file change
> exit
```

A green suite means all three representations agree: definitions, surfaces, and projected
Scala.

---

## 4. Project a definition into Scala

Build the JAR once, and again after any source change:

```bash
sbt assembly        # -> target/scala-2.13/draco-<version>.jar
```

Then the smallest end-to-end loop — a new measurement type in the `Base` domain.

**Write the definition** at `src/main/resources/draco/base/Celsius.json`:

```json
{
  "typeName": { "name": "Celsius", "namePackage": ["draco", "base"] },
  "dracoAspect": {
    "derivation": [
      { "name": "Cardinal", "namePackage": ["draco", "base"], "typeParameters": ["Double"] }
    ],
    "factory": {
      "kind": "Factory",
      "valueType": "Celsius",
      "parameters": [ { "kind": "Parameter", "name": "value", "valueType": "Double" } ]
    }
  },
  "domainAspect": { "typeName": { "name": "Base", "namePackage": ["draco", "base"] } }
}
```

**Project it:**

```bash
bin/draco-gen generate draco/base/Celsius.json > src/main/scala/draco/base/Celsius.scala
```

**Emit the surface** (`DrakeCLI` has no `bin/` wrapper yet):

```bash
java -cp target/scala-2.13/draco-*.jar draco.DrakeCLI emit src/main/resources --write
```

**Register it** by adding `"Celsius"` to `Base.json`'s `domainAspect.elementTypeNames`,
then re-projecting `Base` the same way.

**Verify:**

```bash
bin/draco-gen verify draco/base/Celsius.json    # one type
sbt test                                        # all three gates
```

### What the Scala projection looks like

A type becomes a `trait` plus a companion `object`. The companion carries the type's own
`typeDefinition` (loaded at runtime from the definition), a `dracoType`, a `domainType`,
a factory `apply`, and a `Null`.

Two Scala-specific consequences worth knowing, both listed as residues in `README.md`:

- **Every companion member is `lazy val`.** Scala 2's application-object initialization
  defers eager values, which reads as null across objects. The projection enforces this.
- **Codecs are derived**, using the host JSON library's encoder/decoder pair, for types
  whose factory parameters are all encodable.

---

## 5. Run and use

```bash
sbt "runMain draco.CLI"                            # via sbt
java -cp target/scala-2.13/draco-*.jar draco.CLI   # or from the jar
```

In Scala, a projected type is used directly:

```scala
val c = Celsius(21.5)
c.value                 // 21.5
Celsius.typeDefinition  // the definition, loaded at runtime
Celsius.dracoType       // Type[Celsius]
```

Definitions are resolved through a `DefinitionPath` whose default roots come from the
JVM classpath. A name found at more than one root is a hard error, not a silent choice.

---

## 6. Command reference

**`bin/draco-gen`** — projection CLI (wraps `draco.GeneratorCLI` in the assembled jar):

| Command | Does |
|---|---|
| `generate <path>` | project one definition to stdout |
| `generate-multi <paths…>` | project a family into one file |
| `compile <path>` / `compile-multi <paths…>` | project and compile, reporting errors |
| `inspect <path>` | show the loaded definition |
| `discover <path>` | rebuild a domain's member list from its directory |
| `verify <path>` | project and diff against the checked-in source |

**`draco.DrakeCLI`** — surface CLI, no wrapper yet:

| Command | Does |
|---|---|
| `emit <root> [--write]` | emit `.drake` surfaces for every definition under a root |
| `parse <path>` | parse one surface back to a definition |
| `check <root>` | round-trip every surface under a root |

**`bin/draco-sc`** — runs a script from `src/mods/scala/scripts/` via scala-cli against
the local jar: `derivation-chain`, `diff-type`, `inspect-type`, `list-domain`,
`list-domains`, `who-extends`.

```bash
bin/draco-sc list-domains
bin/draco-sc who-extends DracoType
```

> Both `bin/` tools need the assembled jar. `no draco-*.jar found` means run
> `sbt assembly`. On Windows, use Git Bash or WSL.

---

## Troubleshooting

- **`bad constant pool index` / odd classfile errors** — wrong Java. `java -version` must
  say **17**. macOS: `export JAVA_HOME="$(/usr/libexec/java_home -v 17)"`; elsewhere point
  `JAVA_HOME` at Temurin 17. Homebrew's `sbt` otherwise pulls the newest JDK.
- **First run is slow** — downloading the compiler and dependencies into `~/.sbt` and
  `~/.cache/coursier`. Later runs are fast.
- **`no draco-*.jar found`** — run `sbt assembly`.
- **`permission denied` from `bin/…`** — `bash bin/draco-gen …`, or `chmod +x bin/*`.
- **sbt can't find a JDK** — set `JAVA_HOME`, ensure `java` is on `PATH`, open a fresh
  terminal.
- **A gate fails after you edit one file** — a definition is three artifacts. Change
  `X.json`, and `X.drake` and `X.scala` must follow. That is what the gate is telling you.

---

## Optional: an IDE

The CLI is enough for everything above. If you prefer an editor, **IntelliJ IDEA** with
the **Scala plugin** imports the sbt build directly — open the project folder and let it
import. Set the project SDK to **Java 17**.
