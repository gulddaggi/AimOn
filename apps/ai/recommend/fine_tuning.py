import os
import torch
from datasets import load_dataset
from transformers import (
    AutoModelForCausalLM,
    AutoTokenizer,
    BitsAndBytesConfig,
    AutoTokenizer,
    TrainingArguments,
    pipeline,
)
from peft import LoraConfig, PeftModel, prepare_model_for_kbit_training
from trl import SFTTrainer
from datasets import Dataset
import pandas as pd


# 텍스트 정의
text = """### Instruction: What are the stages of the 2025 EWC Valorant Tournament? ### Assistant: Key Points
- After the tournament begins, teams compete through different rounds to determine the champion.
- There are several ways a team can progress: through upper bracket wins, lower bracket recovery, or group stage dominance.
- Teams may rise from the play-ins, dominate in groups, or pull off upsets in the playoffs.
- The following stages were used for the 2025 EWC Valorant Tournament:
  - Stage 0 (Play-In Stage)
  - Stage I (Group Stage)
  - Stage II (Quarterfinals)
  - Stage III (Semifinals)
  - Stage IV (Finals)
  
After the tournament begins, teams compete through different rounds to determine the champion.
The process of advancing through the stages is based on match victories in a double-elimination format. These rounds filter out lower-performing teams and determine top contenders for the title.

There are several ways a team can progress:
- Upper bracket: Teams who continue winning remain in the winner’s bracket and get direct access to later rounds.
- Lower bracket: Teams who lose in early rounds may fight their way back through the lower bracket.
- Play-In and Group Stage: Performance in earlier stages heavily influences seeding and momentum.

Teams may rise from the play-ins, dominate in groups, or pull off upsets in the playoffs.
Major upsets, lower-bracket runs, or dominant sweeps all shaped the story of the 2025 EWC.

The following stages were used for the 2025 EWC Valorant Tournament:

Stage 0 (Play-In Stage):
Lesser-known or lower-ranked teams competed for a limited number of group stage slots. Some teams were eliminated here, while others earned a chance to challenge top-tier teams.

Stage I (Group Stage):
16 teams were divided into 4 groups. Each group conducted round-robin matches. Top two from each group advanced to the playoffs.
Example: Heretics advanced as 2nd seed from Group B.

Stage II (Quarterfinals):
The top 8 teams clashed in single-elimination matches. Strong contenders like Gen.G and FNATIC began showcasing their peak form.
Example: FNATIC defeated BLG, while Heretics upset EDG.

Stage III (Semifinals):
Four teams—FNATIC, Heretics, Paper Rex, Gen.G—battled in high-stakes matches.
- Heretics defeated Paper Rex
- FNATIC beat Gen.G in a close 2-1 series

Stage IV (Finals):
The grand finals featured Team Heretics vs FNATIC.
Heretics reverse-swept FNATIC 3-2, claiming their first ever major title.
- They were down 0-2, but came back to win 3 straight maps.
- Map 5 (Ascent) ended 13-10 in favor of Heretics.
- Gen.G secured 3rd place after sweeping Paper Rex."
"""

# DataFrame 생성
df = pd.DataFrame({'text': [text]})

# Hugging Face Dataset으로 변환
dataset = Dataset.from_pandas(df)

# Model
base_model = "microsoft/phi-2"
new_model = "phi-2-medquad"

# Tokenizer
tokenizer = AutoTokenizer.from_pretrained(base_model, use_fast=True)
tokenizer.pad_token = tokenizer.unk_token
tokenizer.padding_side = "right"

# Quantization configuration
bnb_config = BitsAndBytesConfig(
    load_in_4bit=True,
    bnb_4bit_quant_type="nf4",
    bnb_4bit_compute_dtype=torch.float16,
    bnb_4bit_use_double_quant=False,
)

# Load base moodel
model = AutoModelForCausalLM.from_pretrained(
    base_model,
    quantization_config=bnb_config,
    trust_remote_code=True,
    device_map={"": 0}
)

model.config.use_cache = False
model.config.pretraining_tp = 1

# LoRA configuration
peft_config = LoraConfig(
    r= 64,          
    lora_alpha= 16,
    lora_dropout=0.05, #0.1
    bias="none",
    task_type="CAUSAL_LM",
   #target_modules= ["Wqkv", "out_proj"] #["Wqkv", "fc1", "fc2" ] # ["Wqkv", "out_proj", "fc1", "fc2" ]
)

# Set training arguments
training_arguments = TrainingArguments(
    output_dir = "./results",
    num_train_epochs = 1,
    fp16 = False,
    bf16 = False,
    per_device_train_batch_size = 4,
    per_device_eval_batch_size = 4,
    gradient_accumulation_steps = 1,
    gradient_checkpointing = True,
    max_grad_norm = 0.3,
    learning_rate = 2e-4,
    weight_decay = 0.001,
    optim = "paged_adamw_32bit",
    lr_scheduler_type = "cosine",
    max_steps = -1,
    warmup_ratio = 0.03,
    group_by_length = True,
    save_steps = 0,
    logging_steps = 25,
)

# Set supervised fine-tuning parameters
trainer = SFTTrainer(
    model=model,
    args=training_arguments,
    train_dataset=dataset,
    peft_config=peft_config,
)
# Train model
trainer.train()

# Save trained model
trainer.model.save_pretrained(new_model)
tokenizer.save_pretrained(new_model)