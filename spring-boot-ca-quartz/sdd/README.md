# Spec-Driven Development (SDD) - AI-Assisted Methodology

This directory contains all artifacts for the **AI-assisted Spec-Driven Development (SDD)** methodology used to develop the Spring Boot Carbon-Aware Quartz starter.

## Overview

This module was developed using a rigorous **Spec-Driven Development** approach, where **specifications are written first** before any code is created. Every feature, component, and behavior is thoroughly documented in technical specifications that serve as the single source of truth for implementation.

The development process leverages **AI assistance** to accelerate implementation while maintaining human oversight for architectural decisions, business logic, and quality assurance.

## What is Spec-Driven Development?

Spec-Driven Development is a software development methodology that prioritizes:

1. **Specifications First**: Write detailed technical specifications before implementation
2. **Traceability**: Every line of code can be traced back to a requirement
3. **Verification**: Tests are designed based on specifications
4. **Documentation**: Documentation drives development, never becomes outdated
5. **Consistency**: Every feature follows the same rigorous process

## AI-Assisted Development Workflow

This project demonstrates how AI can enhance the SDD process while maintaining high standards of quality and maintainability.

### The 6-Step Process

```
┌─────────────────────────────────────────────────────────────────┐
│                    SDD Development Cycle                        │
└─────────────────────────────────────────────────────────────────┘

1. Write Specification     →  Human writes detailed spec
   └─ sdd/specs/*.md          (AI can assist with structure)

2. Update Acceptance       →  Human defines "done" criteria
   └─ acceptence-criteria.md  (AI can assist with formatting)

3. Adjust Architecture     →  AI generates/updates diagrams
   └─ architecture.md         Human reviews decisions

4. Generate Task Board     →  AI creates implementation tasks
   └─ task-board_*.md         Human validates ordering

5. Implement Code          →  AI generates implementation
   └─ src/main/java/...       Human reviews quality

6. Update Documentation    →  AI updates user docs
   └─ README.md, CHANGELOG.md Human ensures clarity
```

### Step 1: Create/Adjust Feature Specification

**Location**: [`specs/`](./specs)

**Primary Responsibility: 👤 Human** (AI can assist with structure and formatting)

Write detailed technical specifications for new features following this structure:

#### Specification Template

```markdown
# Feature Specification: [Feature Name]

| Metadata          | Value |
| :---              | :--- |
| **Feature Name**  | [Short descriptive name] |
| **Spec Version**  | [e.g., 1.0.0] |
| **Status**        | [Draft/Approved/Implemented] |
| **Target Module** | `spring-boot-ca-quartz` |

## 1. Description
[Brief description of what this feature does and why it's needed]

## 2. Included Subfeatures
- **[Subfeature 1]**: Description
- **[Subfeature 2]**: Description

## 3. Explicitly NOT Included
- **[Out of scope item 1]**: Why it's not included
- **[Out of scope item 2]**: Why it's not included

## 4. Technical Details
- **Package**: [Package name]
- **Key Classes**: [List main classes]
- **Configuration**: [Properties or annotations]
- **Logic**: [Implementation approach]

## 5. Test Strategy
- Reference specific test cases from test-specification.md
```

**Example**: [`specs/01_activation.md`](specs/01_activation.md) - Annotation-based activation

**Role Split**:
- 👤 **Human**: Writes the specification, defines requirements, business logic, and constraints
- 🤖 **AI**: Can assist with structuring the document, ensuring consistency, and checking completeness

**AI Assistance Prompt**:
```
"Help me create a specification for [feature] following the template in sdd/README.md.
I'll provide the requirements and you help structure it."
```

### Step 2: Update Acceptance Criteria

**Location**: [`acceptence-criteria.md`](acceptence-criteria.md)

**Primary Responsibility: 👤 Human** (AI can assist with formatting)

Define functional and technical acceptance criteria:

- What constitutes "done" for this feature
- Verification methods
- Testable requirements
- Quality gates

**Format**:
```markdown
### [Feature Name] (v[Version])
#### Functional
- [ ] Provide [specific functionality]
- [ ] Support [specific capability]
- [ ] Ensure [specific behavior]

#### Technical
- [ ] Generate [artifact]
- [ ] Include [documentation]
- [ ] Pass [quality gate]
```

**Role Split**:
- 👤 **Human**: Defines what "done" means, business requirements, and quality standards
- 🤖 **AI**: Helps organize criteria, suggests verification methods, ensures completeness

**AI Assistance Prompt**:
```
"Help me create acceptance criteria for [feature] based on the specification
in sdd/specs/[N]_[feature].md. Suggest testable requirements."
```

**Practical Example**:

Add to `acceptence-criteria.md`:

```markdown
### Custom Strategies (v1.1.0)

#### Functional
- [ ] Provide TimeShiftingStrategy interface for custom implementations
- [ ] Support strategy bean auto-detection (single bean, no configuration needed)
- [ ] Support @Primary strategy selection (multiple beans with one marked @Primary)
- [ ] Support property-based strategy override (carbon.aware.scheduling.strategy.bean-name)
- [ ] Maintain backward compatibility with default strategy
- [ ] Fail fast with clear error message when multiple strategies exist without @Primary

#### Technical
- [ ] Generate configuration metadata for strategy properties
- [ ] Include unit tests for TimeShiftingStrategy interface
- [ ] Include unit tests for DefaultTimeShiftingStrategy
- [ ] Include integration test IT-06a: Custom strategy bean auto-detection
- [ ] Include integration test IT-06b: @Primary strategy selection
- [ ] Include integration test IT-06c: Property-based strategy override
- [ ] Include integration test IT-06d: Fallback to default strategy
- [ ] Pass build with zero Javadoc warnings
```

### Step 3: Adjust Architecture Documentation

**Location**: [`architecture.md`](architecture.md)

Update system architecture documentation:

- Component diagrams showing interactions
- Data flows and lifecycle
- Integration patterns
- Technology stack decisions

**Role Split**:
- 👤 **Human**: Makes architectural decisions, reviews for soundness
- 🤖 **AI**: Generates/updates diagrams and documentation

**AI Prompt**:
```
"Update architecture.md to include TimeShiftingStrategy as a pluggable component
used by TimeShiftingTriggerListener, with similar resolution logic to CarbonForecastApi."
```

Review the generated architecture changes for correctness.

### Step 4: Generate Task Board

**Location**: [`task-board_20260224_1.2.0.md`](task-board_20260224_1.2.0.md)

AI combines architecture, acceptance criteria, and specs to create:

- Ordered, actionable implementation tasks
- Verification checkpoints for each task
- Breaking complex features into manageable steps
- Dependencies and prerequisites

**Role Split**:
- 👤 **Human**: Validates task ordering and identifies missing steps
- 🤖 **AI**: Generates comprehensive task breakdown

**AI Prompt**:
```
"Generate a task-board markdown file (Filename: `task-board_[yyyyMMdd]_[releaseVersion].md`) based on
sdd/specs/[N]_[feature].md, considering the existing architecture in architecture.md
and acceptance-criteria.md."
```

Review task ordering and add any missing steps.

### Step 5: AI-Driven Code Generation

> Before starting this step, it is helpful to ask the AI if there is anything missing for a successful and reproducible implementation of the feature.
> Provide the adjusted or new created files from Step 1–4
> 
> **AI-Prompt:** 

```
"Analyse the following files:
- sdd/specs/[N]_[feature].md
- architecture.md
- acceptence-criteria.md
- task-board_[yyyyMMdd]_[releaseVersion].md

Is there anything missing for a successful and reproducible implementation of [feature] with the SDD approach?"
```

AI implements features following the task board:

- Complete implementation code
- Comprehensive error handling
- Unit and integration tests
- Complete Javadoc documentation
- Configuration metadata

**Role Split**:
- 👤 **Human**: Reviews code quality, test coverage, edge cases
- 🤖 **AI**: Generates implementation following specifications

**AI Prompt** (for each task):
```
"Implement task N from the task board: [task description]
Follow the specification in specs/06_custom_strategies.md"
```

Review each implementation:
- ✅ Follows the specification
- ✅ Includes comprehensive tests
- ✅ Has complete Javadoc
- ✅ Handles edge cases

### Step 6: Update CHANGELOG and README

AI updates documentation to reflect new features:

- Maintains version history in CHANGELOG
- Adds usage examples to README
- Updates configuration reference
- Documents breaking changes

**Role Split**:
- 👤 **Human**: Ensures clarity and completeness for end users
- 🤖 **AI**: Generates documentation updates

**AI Prompt**:
```
"Update CHANGELOG.md with the changes made on this branch.
Update README.md if necessary. If the branch contains breaking changes,
add a guide to migrate from the previous version."
```

Review documentation for clarity and completeness.

## Directory Structure

```
sdd/
├── README.md                       # This file - SDD methodology guide
├── acceptence-criteria.md          # Functional & technical requirements
├── architecture.md                 # System design and component interaction
├── specs.md                        # Glossary and cross-cutting concerns
├── specs/                          # Feature-specific technical specs
│   ├── 01_activation.md           # @EnableCarbonAwareScheduling
│   ├── 02_configuration.md        # Type-safe properties
│   ├── 03_quartz_integration.md   # Scheduler customization
│   ├── 04_dependency_management.md # Bean resolution strategy
│   └── 05_forecast_providers.md   # OpenData provider integration
├── task-board_1.2.0.md            # v1.0.0 implementation tasks
└── test-specification.md           # Test cases and verification strategy
```

## Benefits of AI-Powered SDD

### For Development

- ✅ **Consistency**: Every feature follows the same rigorous process
- ✅ **Speed**: AI accelerates implementation without sacrificing quality
- ✅ **Quality**: Comprehensive testing and documentation from day one
- ✅ **Traceability**: Clear path from requirement → spec → implementation → test
- ✅ **Maintainability**: Well-documented code with clear architectural decisions

### For Collaboration

- ✅ **Clear Communication**: Specs prevent ambiguity and miscommunication
- ✅ **Review Efficiency**: Humans review specs, not just code
- ✅ **Onboarding**: New developers understand the "why" behind every component
- ✅ **Knowledge Retention**: Design decisions are documented, not lost

### For AI Integration

- ✅ **AI Efficiency**: AI excels at implementing well-defined specifications
- ✅ **Human Oversight**: Humans focus on high-value decisions
- ✅ **Predictable Output**: Specifications ensure consistent AI-generated code
- ✅ **Verifiable Results**: Tests validate AI implementation against specs

## Extending This Module

Follow the 6-step SDD workflow described above to add new features.

## Best Practices

### Writing Specifications

1. **Be Specific**: Avoid ambiguity, provide concrete examples
2. **Include Rationale**: Explain *why* decisions were made
3. **Define Scope**: Clearly state what's included and excluded
4. **Document Constraints**: List technical limitations and assumptions
5. **Specify Tests**: Define how the feature will be verified

### Working with AI

1. **Review Everything**: Never blindly accept AI-generated code
2. **Validate Logic**: Ensure business logic is correct
3. **Check Edge Cases**: Verify AI handled all scenarios
4. **Test Thoroughly**: Run tests and add missing coverage
5. **Maintain Specs**: Update specs if requirements change

### Version Control

1. **Commit Specs First**: Check in specifications before implementation
2. **Link Issues to Specs**: Reference spec files in commit messages
3. **Update Task Board**: Mark tasks complete as you go
4. **Version Documentation**: Match task board versions to releases

## Quality Gates

Before considering a feature complete:

- [ ] Specification exists and is complete
- [ ] Acceptance criteria defined and achievable
- [ ] Architecture updated if needed
- [ ] Task board generated and followed
- [ ] All tasks marked complete
- [ ] All tests passing (unit and integration)
- [ ] Javadoc complete with no warnings
- [ ] CHANGELOG updated
- [ ] README updated with examples
- [ ] Code reviewed by human
- [ ] Acceptance criteria verified

## Why This Approach Works

### Clear Separation of Concerns

🎯 **Specifications**: Define *what* needs to be built
🤖 **AI Implementation**: Generates *how* it's built
👤 **Human Review**: Validates *quality* and *correctness*

### Documentation as Source of Truth

📋 Specifications drive development
📋 Tests verify specifications
📋 Code implements specifications
📋 Documentation reflects specifications

### Traceability

Every component can be traced:
```
Business Need → Acceptance Criteria → Specification → Task → Implementation → Test
```

### Maintainability

Future developers can:
- Understand design decisions from specs
- Modify features with confidence
- Add features following established patterns
- Onboard quickly with clear documentation

## Tools and Prompts

### Useful AI Prompts

**Generate Task Board:**
```
"Generate a detailed task board for implementing [feature] based on:
- Specification: sdd/specs/[N]_[feature].md
- Architecture: sdd/architecture.md
- Acceptance Criteria: sdd/acceptence-criteria.md

Include verification steps for each task."
```

**Implement Task:**
```
"Implement task [N] from the task board: [task description]
Follow the specification in sdd/specs/[N]_[feature].md
Include:
- Complete implementation
- Unit tests
- Integration tests
- Javadoc documentation"
```

**Review Implementation:**
```
"Review the implementation of [component] against specification
sdd/specs/[N]_[feature].md. Check for:
- Compliance with spec requirements
- Edge case handling
- Test coverage
- Error handling
- Javadoc completeness"
```

**Update Documentation:**
```
"Update CHANGELOG.md and README.md to document [feature] based on:
- Implementation in src/main/java/[path]
- Specification in sdd/specs/[N]_[feature].md
- Tests in src/test/java/[path]"
```

## Related Resources

- [Main Project README](../README.md) - User-facing documentation
- [CHANGELOG](../CHANGELOG.md) - Version history
- [AGENTS.md](../AGENTS.md) - Developer guide
- [Architecture Documentation](architecture.md) - System design
- [Test Specification](./specs/test-specification.md) - Test strategy

---

**This methodology demonstrates how AI can be effectively integrated into professional software development workflows while maintaining high standards of quality, maintainability, and traceability.**

*Developed with 💚 for sustainable and maintainable software*
