# src/modelo/partida.py

class Partida:
    def __init__(self, ano: int, rodada: int, data: str, time_casa: str, time_fora: str, gols_casa: int, gols_fora: int, estadio: str, **kwargs):
        self.ano = ano
        self.rodada = rodada
        self.data = data
        self.time_casa = time_casa
        self.time_fora = time_fora
        self.gols_casa = gols_casa
        self.gols_fora = gols_fora
        self.estadio = estadio
        
        # Atribui os atributos adicionais dinamicamente
        for key, value in kwargs.items():
            setattr(self, key, value)

    def __str__(self):
        return f"Ano {self.ano} Rodada {self.rodada}: {self.time_casa} {self.gols_casa} x {self.gols_fora} {self.time_fora}"

    # Getters para atributos básicos
    def get_ano(self): return self.ano
    def get_rodada(self): return self.rodada
    def get_data(self): return self.data
    def get_time_casa(self): return self.time_casa
    def get_time_fora(self): return self.time_fora
    def get_gols_casa(self): return self.gols_casa
    def get_gols_fora(self): return self.gols_fora
    def get_estadio(self): return self.estadio
    
    # Getters para atributos adicionais, usando getattr() para segurança
    def get_tecnico_casa(self): return getattr(self, 'tecnico_casa', None)
    def get_tecnico_fora(self): return getattr(self, 'tecnico_fora', None)
    def get_stat_posse_de_bola_casa(self): return getattr(self, 'stat_posse_de_bola_casa', None)
    def get_stat_posse_de_bola_fora(self): return getattr(self, 'stat_posse_de_bola_fora', None)
    def get_stat_chutes_casa(self): return getattr(self, 'stat_chutes_casa', None)
    def get_stat_chutes_fora(self): return getattr(self, 'stat_chutes_fora', None)
    def get_stat_chutes_no_alvo_casa(self): return getattr(self, 'stat_chutes_no_alvo_casa', None)
    def get_stat_chutes_no_alvo_fora(self): return getattr(self, 'stat_chutes_no_alvo_fora', None)
    def get_stat_escanteios_casa(self): return getattr(self, 'stat_escanteios_casa', None)
    def get_stat_escanteios_fora(self): return getattr(self, 'stat_escanteios_fora', None)
    def get_stat_faltas_casa(self): return getattr(self, 'stat_faltas_casa', None)
    def get_stat_faltas_fora(self): return getattr(self, 'stat_faltas_fora', None)
    def get_stat_impedimentos_casa(self): return getattr(self, 'stat_impedimentos_casa', None)
    def get_stat_impedimentos_fora(self): return getattr(self, 'stat_impedimentos_fora', None)
    def get_stat_cartao_amarelo_casa(self): return getattr(self, 'stat_cartao_amarelo_casa', None)
    def get_stat_cartao_amarelo_fora(self): return getattr(self, 'stat_cartao_amarelo_fora', None)
    def get_stat_cartao_vermelho_casa(self): return getattr(self, 'stat_cartao_vermelho_casa', None)
    def get_stat_cartao_vermelho_fora(self): return getattr(self, 'stat_cartao_vermelho_fora', None)