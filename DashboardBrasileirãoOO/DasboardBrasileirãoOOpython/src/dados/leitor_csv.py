import pandas as pd
import os
import re

class LeitorCSV:
    @staticmethod
    def carregar_partidas(caminho_arquivo: str):
        try:
            resource_path = os.path.join(os.path.dirname(__file__), '..', '..', 'resources', caminho_arquivo)
            df = pd.read_csv(resource_path, sep=';', encoding='utf-8')

            # ** MUDANÇA: Adiciona a coluna 'ano' ao DataFrame **
            ano = LeitorCSV.extrair_ano_do_nome_arquivo(caminho_arquivo)
            df['ano'] = ano
            
            return df
        except FileNotFoundError:
            print(f"ERRO: O arquivo {caminho_arquivo} não foi encontrado no caminho: {resource_path}")
            return pd.DataFrame()

    @staticmethod
    def extrair_ano_do_nome_arquivo(nome_arquivo: str):
        match = re.search(r'(\d{4})', nome_arquivo)
        if match:
            return int(match.group(1))
        return 0