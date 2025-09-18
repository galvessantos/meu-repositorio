package com.montreal.core.domain.component;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EmailComponent {

    public static String getTemplateEmailNewUser(String username, String name, String linkCreatePassword, String linkAccessSystem) {

        var template = """
        <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <h2>Caro(a) {{NAME}},</h2>
                <p>Seu cadastro no sistema <b>hubRecupera</b> foi efetuado com sucesso!</p>
                <p>Seu login de acesso é: <b>{{USERNAME}}</b></p>
                <p>
                    Para cadastrar sua senha de acesso,
                    <a href={{LINK_CREATE_PASSWORD}}
                       style="color: #4CAF50; text-decoration: none; font-weight: bold;">
                       clique aqui
                    </a>.
                </p>
                <p>Lembrando que sua senha deve atender aos seguintes critérios:</p>
                <ul>
                    <li><b>Tamanho:</b> Entre 4 e 8 caracteres.</li>
                    <li><b>Letras:</b> Inclua pelo menos uma letra maiúscula e uma letra minúscula.</li>
                    <li><b>Números:</b> Adicione pelo menos um número.</li>
                    <li><b>Caracteres especiais:</b> pelo menos um. Aceitos: <code>_</code> (sublinhado), <code>@</code> (arroba) e <code>#</code> (tralha).</li>
                </ul>
                <p>
                    Após cadastrar sua senha,
                    <a href={{LINK_ACCESS_SYSTEM}}
                       style="color: #4CAF50; text-decoration: none; font-weight: bold;">
                       clique aqui
                    </a> para acessar o sistema.
                </p>
                <p>Atenciosamente,<br>hubRecupera</p>
            </body>
        </html>
        """;

        return template.replace("{{NAME}}", name)
                .replace("{{USERNAME}}", username)
                .replace("{{LINK_CREATE_PASSWORD}}", linkCreatePassword)
                .replace("{{LINK_ACCESS_SYSTEM}}", linkAccessSystem);
    }

    public static String getTemplatePasswordReset(String resetUrl) {
        var template = """
                <html lang="pt-BR">
                <body>
                <pre style="font-family: sans-serif; font-size: 14px;">
                Prezado(a) Cliente,

                Para trocar a senha da conta de rede favor acessar o portal abaixo seguindo as seguintes orientações:
                - Mantenha a confidencialidade, garantindo que ela não seja divulgada, incluindo a autoridades e lideranças.
                - Não compartilhe a sua senha. Ela é individual e intransferível.
                - Não anote ou salve sua senha em nenhuma circunstância. Acessos indevidos, serão de sua responsabilidade.
                - Altere a senha sempre que existir qualquer indicação de possível comprometimento da confidencialidade.

                Escolha senhas que contenham no mínimo 03 dos 4 requisitos abaixo:
                - 01 caractere especial ( * %% $ # @ ! & )
                - 01 numeral
                - Letras maiúsculas e minúsculas
                - Mínimo de 8 caracteres.

                Click no link abaixo para ser redirecionado para a tela de redefinição de senha:
                👉 <a href="{{RESET_URL}}">{{RESET_URL}}</a>

                Muito obrigado,

                InfraTI - MIBH
                MIBH Suporte N1
                </pre>
                </body>
                </html>
                """;
        return template.replace("{{RESET_URL}}", resetUrl);
    }
}
