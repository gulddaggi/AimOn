# ✅ 1. 패키지 import
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer, pipeline, logging
from peft import PeftModel

# ✅ 2. 설정
base_model = "microsoft/phi-2"
new_model = "phi-2-medquad"

# ✅ 3. 원래 모델 로드 및 LoRA 병합
model = AutoModelForCausalLM.from_pretrained(
    base_model,
    low_cpu_mem_usage=True,
    return_dict=True,
    torch_dtype=torch.float16,
    device_map={"": 0},
)
model = PeftModel.from_pretrained(model, new_model)
model = model.merge_and_unload()

# ✅ 4. 토크나이저 설정
tokenizer = AutoTokenizer.from_pretrained(base_model, trust_remote_code=True)
tokenizer.pad_token = tokenizer.eos_token
tokenizer.padding_side = "right"

# ✅ 5. 텍스트 생성
logging.set_verbosity(logging.CRITICAL)
pipe = pipeline("text-generation", model=model, tokenizer=tokenizer, max_length=200)

prompt = "### Instruction: What are the treatments for Gastrointestinal Carcinoid Tumors?"
result = pipe(prompt)

print(result[0]['generated_text'])
