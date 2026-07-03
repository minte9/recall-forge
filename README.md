# Recall Forge

## 6. Claude Unit Testing

### 6.1 Claude Code

GitHub Copilot is primarily an IDE assistant.  
Claude Code is primarily a coding agent.  

Claude Code runs from the terminal alongside VS Code.  
You keep VS Code open for editing, but in another terminal you run something like:  

~~~sh
claude
~~~

Then you can give it much larger tasks:  

~~~sh
Analyze the entire project and identify services without unit tests.
~~~

It can spend several minutes working through the task, editing many files and running commands.

So instead of:

~~~sh
You -> Ask
AI -> Answer
AI -> Wait for approve
You -> Ask again
~~~

It's more like:

~~~sh
You -> Give objective

AI
├── Reads project
├── Makes a plan
├── Edits files
├── Runs tests
├── Fixes failures
└── Reports back
~~~

### 6.2 Create account

Go to Claude and create an Anthropic account.   
For Claude Code, you can use a Pro or Max plan.    
Pro is currently listed at $20/month.  


### 6.3 Install Claude Code

In the terminal:

~~~sh
npm install -g @anthropic-ai/claude-code

cd path/to/myproject
claude
~~~


### 6.4 First safe task

Paste this into Claude:

~~~sh
You are working in my RecallForge project.

Goal:
Help me start unit testing safely.

Rules:
- Do not change production code.
- Do not install new packages without asking.
- First inspect the project structure.
- Detect the language, test framework, and existing test conventions.
- Find 5 important modules/classes/functions that currently need unit tests.
- Rank them by business importance and testability.
- Pick the easiest high-value target.
- Create exactly one small unit test file.
- Run only the relevant test command.
- If the test fails, explain why before changing anything.
~~~

~~~sh
● I'll start by inspecting the project structure to understand the language, framework, and existing conventions.
  Listed 1 directory (ctrl+o to expand)

● This is a Gradle/Java project. Let me look deeper at the source structure and existing tests.
  Searched for 2 patterns, read 1 file (ctrl+o to expand)

● No existing tests at all — clean slate. Let me look at the service layer, since business logic there is the
  highest-value testing target.
  Read 5 files, listed 1 directory (ctrl+o to expand) ...

~~~

~~~sh
● Bash(mkdir -p src/test/java/dev/recallforge/service)
  ⎿  Done
● Write(src/test/java/dev/recallforge/service/RepetitionServiceTest.java)
● Bash(mkdir -p src/test/java/dev/recallforge/service)
  ⎿  Done
● Write(src/test/java/dev/recallforge/service/RepetitionServiceTest.java)
● Bash(./gradlew test --tests "dev.recallforge.service.RepetitionServiceTest" --offline 2>&1 | tail -60)
  ⎿  Starting a Gradle Daemon, 1 incompatible and 1 stopped Daemons could not be reused, use --status for details
     > Task :compileJava
     > Task :processResources
     … +10 lines (ctrl+o to expand)
● All 5 tests passed on the first run — no failures to explain ...

~~~


### 6.5. Instruction files

In the root directory create claude/UnitTests.md

~~~markdown
# RecallForge Claude Instructions

## Unit testing workflow

When adding unit tests:

- Do not modify production code unless explicitly approved.
- Do not create log files or report files.
- Prefer behavior tests over implementation-detail tests.
- Add one focused test file or one focused test group at a time.
- Run only the relevant Gradle test, not the full suite unless asked.
- Use offline Gradle mode when possible:
  `./gradlew test --offline`
- After finishing, report only in the terminal:
  - what was tested
  - files changed
  - command run
  - result
  - next recommended target
~~~

Then future prompts can be very short:

~~~sh
Continue adding unit tests. Pick the next highest-value target and follow claude/UnitTests.md
~~~


### 6.6 Recommended structure

You can use multiple Markdown files.  
Claude loads them from the CLAUDE.md (loaded automatically).   

~~~sh
recallforge/
│
├── CLAUDE.md                 ← entry point
│
└── .claude/
    ├── unit-testing.md
    ├── backend.md
    ├── frontend.md
    ├── release.md
    └── code-style.md
~~~

Then CLAUDE.md stays short:

~~~markdown
# RecallForge

General rules:
- Never modify production code without approval.
- Follow existing project conventions.

Additional guides:
- .claude/unit-testing.md
- .claude/backend.md
- .claude/frontend.md
- .claude/code-style.md

Read only the guides relevant to the current task.
~~~

Now you can say:  

~~~sh
Continue writing backend unit tests.
~~~

Claude will see from CLAUDE.md that there is a unit-testing.md and backend.md guide and use those.  

Or later:

~~~sh
Implement a React component.  
~~~


### 6.7 Usage

Go to Settings → Usage.   
Or, while you're in the Claude Code session, type:  

~~~sh
/usage
~~~
~~~sh
Session

Total cost:            $3.29
Total duration (API):  10m 16s
Total duration (wall): 3h 12m 33s
Total code changes:    1146 lines added, 11 lines removed
Usage by model:
    claude-haiku-4-5:  677 input, 18 output, 0 cache read, 0 cache write ($0.0008)
    claude-sonnet-5:  7.7k input, 60.4k output, 5.7m cache read, 106.4k cache write ($3.28)

Current session
█████                                              10% used
Resets 5:30pm (Europe/Bucharest)

Current week (all models)
▌                                                  1% used

~~~

That $3.24 is not your bill.  
This is one of the most confusing parts of Claude Code.  
Here's how to read it:  

    Total cost: $3.24 = the API-equivalent value of the model usage in this session.
    Since you're on a Claude Pro subscription, you are not charged $3.24.
    Instead, that usage is deducted from your Pro plan allowance.

The important numbers for you are these:

    Current session: 10% used
    Resets: 5:30 PM (Europe/Bucharest)


Long conversations accumulate context, which increases session usage.  
Summarize the current repository state and forget the detailed history of this session.  
Or simply start a new Claude session every few modules.  

~~~sh
/exit

claude
~~~