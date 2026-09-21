package com.store.vitrine3d.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;

public enum RecurringFrequency {
    MONTHLY,
    QUARTERLY,
    SEMIANNUAL,
    ANNUAL;
    //Nessa classe estamos definindo a data para qual vai ser agendado o lembrede de novo pagamento.
    //O usuario escolhe o periodo de tempo para o proximo pagamento informando se é Mensal, quaternal, semianual....
    //dueDay-> Dia escolhido pelo usuario ou automatico do dia do pagamento
    public LocalDate nextDate(LocalDate from, int dueDay) {
        int months = switch(this){
            case MONTHLY -> 1;
            case QUARTERLY -> 3;
            case SEMIANNUAL -> 6;
            case ANNUAL -> 12;
        };

        YearMonth target = YearMonth.from(from).plusMonths(months);
        //Precisamos garantir que o dia agendado sempre vai existir naquele mes
        //Para isso precisamos identificar quantos dias o mes tem e escolher sempre
        //O menor entre o dia selecionado e o dia do mes 
        int actualDate = Math.min(dueDay, target.lengthOfMonth());

        return LocalDate.of(target.getYear(), target.getMonth(), actualDate);

    }
}
