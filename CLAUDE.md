---
purpose: workspace index for AI sessions
format: strict key-value
rule: read this file first. read CONTEXT.md second. read PROJECT.md before any code task. then proceed.
---

# WORKSPACE INDEX

project: runelite-pvp-enhancer
platform: RuneLite (OSRS client plugin)
phase: greenfield — no code written yet. start from .ai/backlog.md B001.
status: active
latest_session: none

## State

context_file: CONTEXT.md
project_spec: PROJECT.md
ai_index: .ai/INDEX.md
architecture_map: .ai/architecture.md
backlog: .ai/backlog.md
backlog_history: .ai/backlog-history.md
human_testing: .ai/human-testing.md
human_testing_history: .ai/human-testing-history.md
readme: README.md
rules_dir: .ai/rules/
sessions_dir: .ai/sessions/
design_docs_dir: docs/

## Communication Style

caveman: lite
caveman_rule: caveman lite applies to (1) all AI chat responses and (2) all AI-maintained Tier 1 files. drop filler, no hedging, no pleasantries. keep articles and full sentences. professional and tight. code/commits/PRs/Javadoc written normally. deactivate only if user says "stop caveman" or "normal mode".
lang_rule: all AI-maintained docs in English. code and commits always English. user chat may be Portuguese; AI replies match user language unless writing files.

## AI Protocol

rule_01: read CONTEXT.md before modifying any file
rule_02: after each session update (a) root CONTEXT.md latest_session field, (b) CLAUDE.md latest_session field, (c) .ai/INDEX.md active_session field. Write session file under sessions_dir.
rule_03: read PROJECT.md before any code task touching architecture, plugin structure, overlays, or game interaction
rule_04: load rule files from rules_dir by load_when frontmatter match. priority: explicit load_when > package CONTEXT.md > root CONTEXT.md > architecture.md
rule_05: docs in design_docs_dir are Tier 2 human-prose, full sentences, complete narrative
rule_06: sessions are isolated. load only active session file. completed items move to backlog_history.
rule_07: follow rules/doc-style.md — 3-tier classification. no log sections in Tier 1 docs.
rule_08: when work needs live-client/visual validation the AI cannot do, add a priority-ordered HT item to human_testing. When the user asks to "run/validate HT-NNN" or "start human testing", guide them ONE step at a time, collect the RESULT TEMPLATE, then on PASS move it to human_testing_history + mark the linked B-item validated; on FAIL open a B### in backlog with the report as evidence and leave the HT item queued.

## Project Structure

language: Java 11
build: Gradle (RuneLite plugin template)
runtime: RuneLite client (open-source, latest stable)
entry_point: com.knz.pvpenhancer.PvpEnhancerPlugin extends Plugin
main_packages:
  plugin/    core plugin class, config, and bootstrapping
  overlay/   all Overlay subclasses (one per rendering concern)
  util/      shared stateless helpers (combat math, gear value, etc.)

## Build

command: ./gradlew build
output: build/libs/pvp-enhancer-<version>.jar
prerequisite: JDK 11+, RuneLite open-source setup
