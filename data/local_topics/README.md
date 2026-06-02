# RecallForge

RecallForge is a spaced-repetition knowledgebase agent.  
It imports learning notes from markdown, asks questions, evaluates answers, and tracks weak topics.

## Pipeline Model

A pipeline model processes data in steps.  
Each step receives input, transforms it, and passes the result to the next step.  
Pipelines are useful because they make complex workflows easier to understand and debug.

## Agent Loop

An agent loop usually follows this pattern:  

Think → Act → Observe → Repeat.

The agent decides what to do, uses a tool or action, observes the result, and continues.  

## Tool Calling

Tool calling allows an LLM to use external functions.  
Instead of only generating text, the model can ask the application to run specific actions such as searching files, reading data, or calling APIs.

## Spaced Repetition

Spaced repetition is a learning technique where topics are reviewed at increasing intervals.  
Topics that are poorly remembered are repeated sooner.  
Topics that are well remembered are repeated later.