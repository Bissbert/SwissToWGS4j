#!/usr/bin/env python3
"""Compile the public API probe and print documentation measurements.

Only the Python standard library is used. A JDK is required because this
script compiles and runs the Java sources under test.
"""

from pathlib import Path
import re
import subprocess
import sys


ROOT = Path(__file__).resolve().parents[1]
CLASSES = ROOT / "target" / "docs-measurement-classes"
SOURCE_ROOT = ROOT / "src" / "main" / "java"


def run(command):
    return subprocess.run(
        command,
        cwd=ROOT,
        check=True,
        text=True,
        capture_output=True,
    )


def java(*args):
    result = run(["java", "-cp", str(CLASSES), "Probe", *args])
    return result.stdout.strip()


def compiler_warning_count(stderr):
    return sum(1 for line in stderr.splitlines() if re.search(r"warning", line))


def main():
    CLASSES.mkdir(parents=True, exist_ok=True)
    sources = sorted(SOURCE_ROOT.rglob("*.java")) + [ROOT / "tools" / "Probe.java"]
    compile_result = run(
        ["javac", "-Xlint:all", "-d", str(CLASSES), *map(str, sources)]
    )

    print("compiler.warning_count=" + str(compiler_warning_count(compile_result.stderr)))
    print("quickstart:")
    print(java("quickstart"))
    print("bugs:")
    print(java("bugs"))
    print("roundtrip:")
    print(java("roundtrip", "10000"))
    print("shift:")
    print(java("shift", "10000"))
    print("absolute_accuracy=not measured")


if __name__ == "__main__":
    try:
        main()
    except (OSError, subprocess.CalledProcessError) as error:
        print(error, file=sys.stderr)
        sys.exit(1)
