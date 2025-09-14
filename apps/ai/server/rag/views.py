# rag/views.py
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from .rag_engine import answer_auto, check_token  # ✅ AUTO만 사용

class AskView(APIView):
    def post(self, request):
        # (선택) 내부 토큰 검사 - Spring만 접근 가능하게
        token = request.headers.get("X-Internal-Token")
        if not check_token(token):
            return Response({"error": "unauthorized"}, status=status.HTTP_401_UNAUTHORIZED)

        q = (request.data.get("question") or "").strip()
        if not q:
            return Response({"error": "question is required"}, status=status.HTTP_400_BAD_REQUEST)

        try:
            top_k_raw = request.data.get("top_k")
            top_k = int(top_k_raw) if top_k_raw is not None else None
        except ValueError:
            return Response({"error": "top_k must be int"}, status=status.HTTP_400_BAD_REQUEST)

        try:
            result = answer_auto(q, top_k=top_k)  # ✅ 항상 AUTO 경로
            return Response({"answer": result.get("answer", "")}, status=status.HTTP_200_OK)
        except Exception as e:
            # 운영 시 로깅 권장
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

class HealthView(APIView):
    def get(self, request):
        return Response({"status": "ok"}, status=status.HTTP_200_OK)
