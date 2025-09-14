import json

data = json.load(open("DB_JSON/guide.json", "r", encoding="utf-8"))
print(data["세부 정보"]["시스템"]["인게임"]["요원 선택"])