class Time:
    def __init__(self, nome: str):
        self._nome = nome
        self._pontos = 0
        self._jogos = 0
        self._vitorias = 0
        self._empates = 0
        self._derrotas = 0
        self._gols_pro = 0
        self._gols_contra = 0
        self._saldo_gols = 0

    @property
    def nome(self):
        return self._nome

    @property
    def pontos(self):
        return self._pontos

    @property
    def jogos(self):
        return self._jogos

    @property
    def vitorias(self):
        return self._vitorias

    @property
    def empates(self):
        return self._empates

    @property
    def derrotas(self):
        return self._derrotas

    @property
    def gols_pro(self):
        return self._gols_pro

    @property
    def gols_contra(self):
        return self._gols_contra

    @property
    def saldo_gols(self):
        return self._saldo_gols

    def adicionar_vitoria(self, gols_pro: int, gols_contra: int):
        self._jogos += 1
        self._vitorias += 1
        self._pontos += 3
        self._gols_pro += gols_pro
        self._gols_contra += gols_contra
        self._atualizar_saldo_gols()

    def adicionar_empate(self, gols: int):
        self._jogos += 1
        self._empates += 1
        self._pontos += 1
        self._gols_pro += gols
        self._gols_contra += gols

    def adicionar_derrota(self, gols_pro: int, gols_contra: int):
        self._jogos += 1
        self._derrotas += 1
        self._gols_pro += gols_pro
        self._gols_contra += gols_contra
        self._atualizar_saldo_gols()

    def _atualizar_saldo_gols(self):
        self._saldo_gols = self.gols_pro - self.gols_contra