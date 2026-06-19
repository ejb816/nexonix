# Getting Started

A from-scratch guide for a newcomer on a **fresh Windows, macOS, or Linux/Unix
machine**: install the tools, clone Draco, and run any test, app, or tool — using
either `sbt` or the native shell.

By the end you will have **Git**, a **Java 17** runtime, **sbt** (the build tool),
and **scala-cli** (for the script tools), a local clone, and a green test run.

> **The one thing that matters most: use Java 17.** Draco is pinned to JDK 17. A
> newer JDK (21, 23, 25, …) fails with errors like `bad constant pool index`. Every
> section below installs and pins **Java 17** on purpose.

---

## 1. Install the tools

You need four things: **Git**, **JDK 17**, **sbt**, **scala-cli**. The Scala trio
(JDK, sbt, scala-cli) is easiest in one shot with **[Coursier](https://get-coursier.io)**
(`cs`), the official Scala installer; Git you install separately.

### macOS

Using [Homebrew](https://brew.sh) (install it first if you don't have it):

```bash
brew install git
brew install coursier/formulas/coursier
cs setup --jvm temurin:17 --yes          # installs sbt, scala-cli, scala + a Java 17
```

Open a **new** terminal, then make Java 17 the default (zsh):

```bash
echo 'export JAVA_HOME="$(/usr/libexec/java_home -v 17)"' >> ~/.zshrc
source ~/.zshrc
```

### Linux / Unix

```bash
# Git — Debian/Ubuntu shown; use your distro's package manager otherwise
sudo apt-get update && sudo apt-get install -y git

# Coursier (x86_64), then the Scala toolchain pinned to Java 17
curl -fL "https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz" | gzip -d > cs
chmod +x cs
./cs setup --jvm temurin:17 --yes
```

Coursier adds itself and Java 17 to your shell profile — open a **new** terminal so
`java`, `sbt`, and `scala-cli` are on your `PATH`. (For ARM/other architectures, see
the installer links at [get-coursier.io](https://get-coursier.io/docs/cli-installation).)

### Windows

Pick one path.

**A — winget (built into Windows 10/11), in PowerShell:**

```powershell
winget install --id Git.Git -e
winget install --id EclipseAdoptium.Temurin.17.JDK -e
winget install --id VirtusLab.ScalaCLI -e
winget install --id sbt.sbt -e            # if unavailable, use path B for sbt
```

**B — Coursier:** download `cs-x86_64-pc-win32.zip` from the
[Coursier releases](https://github.com/coursier/launchers/releases), unzip it, then:

```powershell
.\cs.exe setup --jvm temurin:17 --yes
```

> **Windows + the `bin/` tools:** `bin/draco-gen` and `bin/draco-sc` are Bash
> scripts — run them from **Git Bash** (ships with Git for Windows) or **WSL**.
> `sbt` and `scala-cli` themselves work fine in PowerShell.

### Verify

Open a **fresh** terminal and check all four. Java must report **17**:

```bash
git --version
java -version        # -> openjdk version "17.x.x"   <- must be 17
sbt --version        # -> sbt ... 1.12.9   (first run downloads sbt)
scala-cli version
```

---

## 2. Clone the project

```bash
git clone https://github.com/ejb816/nexonix.git
cd nexonix
```

---

## 3. Run the tests (via sbt)

The first `sbt` command downloads sbt, Scala 2.13.16, and all dependencies — expect a
few minutes **once**, then everything is cached.

```bash
sbt compile                                            # compile everything
sbt test                                               # run the whole suite
sbt "testOnly draco.primes.PrimesRulesTest"            # one test class
sbt "testOnly domains.world.* domains.aerial.*"        # a group (World + Aerial example)
sbt "testOnly draco.primes.PrimesRulesTest -- -z PrimesFromNaturalSequence"  # one test by name
```

Or use the interactive shell to avoid repeated JVM startup:

```bash
sbt
> compile
> test
> testOnly domains.world.*
> ~compile          # recompile automatically on every file change
> exit
```

---

## 4. Run the app and the CLI tools (native shell)

Draco ships a fat-JAR CLI (`draco.CLI`) and two shell wrappers. **Build the jar once**
(and again after any source change):

```bash
sbt assembly        # produces target/scala-2.13/draco-<version>.jar
```

### The Draco REPL / CLI

```bash
sbt "runMain draco.CLI"                            # via sbt (no assembly needed)
java -cp target/scala-2.13/draco-*.jar draco.CLI   # or straight from the jar
```

### `bin/draco-gen` — the code-generator CLI

A thin wrapper around `draco.GeneratorCLI` in the assembled jar.

```bash
bin/draco-gen                 # prints its subcommands (generate / compile / inspect / discover / verify / from-yaml / to-yaml ...)
bin/draco-gen inspect <TypeName>
```

### `bin/draco-sc` — run a mod script

Runs a script from `src/mods/scala/scripts/*.scala` via scala-cli against the local jar.

```bash
bin/draco-sc                          # usage + lists the available scripts
bin/draco-sc inspect-type Primal draco
bin/draco-sc list-domains
```

Available scripts: `derivation-chain`, `diff-type`, `inspect-type`, `list-domain`,
`list-domains`, `who-extends`.

> Both `bin/` tools need the assembled jar. If you see `no draco-*.jar found`, run
> `sbt assembly` first. On Windows, run them from Git Bash or WSL.

---

## Troubleshooting

- **`bad constant pool index` / odd classfile errors** — you're on the wrong Java.
  `java -version` must say **17**. Re-pin: macOS
  `export JAVA_HOME="$(/usr/libexec/java_home -v 17)"`; elsewhere point `JAVA_HOME`
  at your Temurin 17 install. (Homebrew's `sbt` otherwise tends to pull the newest JDK.)
- **First `sbt` / `scala-cli` run is slow** — it's downloading the compiler and
  dependencies into a local cache (`~/.sbt`, `~/.cache/coursier`); later runs are fast.
- **`no draco-*.jar found`** from `bin/draco-*` — run `sbt assembly`.
- **`bin/draco-* : permission denied`** — run as `bash bin/draco-gen …`, or
  `chmod +x bin/*`; on Windows use Git Bash / WSL.
- **sbt can't find a JDK** — ensure `JAVA_HOME` is set and `java` is on `PATH`, then
  open a fresh terminal.

---

## Optional: an IDE

CLI is enough to build, test, and run everything above. If you prefer an editor,
**IntelliJ IDEA** with the **Scala plugin** imports the sbt build directly (open the
project folder; let it import). Make sure the project SDK is **Java 17**.
