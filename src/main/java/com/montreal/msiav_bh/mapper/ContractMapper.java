package com.montreal.msiav_bh.mapper;

import com.montreal.msiav_bh.dto.response.QueryDetailResponseDTO;
import com.montreal.msiav_bh.entity.*;
import com.montreal.msiav_bh.enumerations.AgencyType;
import com.montreal.msiav_bh.repository.*;
import com.montreal.msiav_bh.utils.AddressParser;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ContractMapper {

    private final AddressParser addressParser;
    private final AddressRepository addressRepository;
    private final DebtorRepository debtorRepository;
    private final CreditorRepository creditorRepository;
    private final DetranRepository detranRepository;
    private final NotaryRepository notaryRepository;

    public ContractMapper(AddressParser addressParser,
                          AddressRepository addressRepository,
                          DebtorRepository debtorRepository,
                          CreditorRepository creditorRepository,
                          DetranRepository detranRepository,
                          NotaryRepository notaryRepository) {
        this.addressParser = addressParser;
        this.addressRepository = addressRepository;
        this.debtorRepository = debtorRepository;
        this.creditorRepository = creditorRepository;
        this.detranRepository = detranRepository;
        this.notaryRepository = notaryRepository;
    }

    public Contract toEntity(QueryDetailResponseDTO.Data data, List<VehicleCache> existingVehicles, Contract existingContract) {
        if (data == null) {
            return null;
        }

        Contract contract = existingContract != null ? existingContract : new Contract();

        // Mapear dados do contrato
        if (data.contrato() != null) {
            QueryDetailResponseDTO.Contrato dto = data.contrato();
            contract.setNumero(dto.numero());
            contract.setProtocolo(dto.protocolo());
            contract.setDataContrato(dto.dataContrato());
            contract.setDataPedido(dto.dataPedido());
            contract.setDataNotificacao(dto.dataNotificacao());
            contract.setDataDecursoPrazo(dto.dataDecursoPrazo());
            contract.setMunicipioContrato(dto.municipioContrato());
            contract.setValorDivida(dto.valorDivida() != null ? dto.valorDivida().toString() : null);
            contract.setValorLeilao(dto.valorLeilao());
            contract.setDescricao(dto.descricao());
            contract.setValorParcela(dto.valorParcela());
            contract.setTaxaJuros(dto.taxaJuros());
            contract.setCertidaoBuscaApreensao(dto.certidaoBuscaApreensao());
            contract.setDataPrimeiraParcela(dto.dataPrimeiraParcela());
            contract.setQuantidadeParcelasPagas(dto.quantidadeParcelasPagas());
            contract.setQuantidadeParcelasAbertas(dto.quantidadeParcelasAbertas());
            contract.setProtocoloBa(dto.protocoloBa());
            contract.setDataCertidao(dto.dataCertidao());
            contract.setDataRestricao(dto.dataRestricao());
            contract.setNumeroRestricao(dto.numeroRestricao());
            contract.setDataBaixaRestricao(dto.dataBaixaRestricao());
            contract.setNsu(dto.nsu());
            contract.setIndicadorRestricaoCirculacao(dto.indicadorRestricaoCirculacao());
        }

        // Mapear credor com verificação de existência
        if (data.credor() != null) {
            Creditor creditor;
            if (contract.getId() != null) {
                creditor = creditorRepository.findByContratoId(contract.getId())
                        .orElseGet(() -> toCreditorEntity(data.credor()));
            } else {
                creditor = toCreditorEntity(data.credor());
            }

            // Atualizar campos
            creditor.setName(data.credor().nome());
            creditor.setCnpj(data.credor().cnpj());
            creditor.setEmail(data.credor().email());
            creditor.setStateRegistration(data.credor().inscricaoEstadual());
            creditor.setAddress(data.credor().endereco());
            creditor.setPhone(data.credor().telefone());
            creditor.setContrato(contract);
            contract.setCredor(creditor);
        }

        // Mapear devedores
        if (data.devedores() != null) {
            List<Debtor> debtors = data.devedores().stream()
                    .map(this::toDebtorEntity)
                    .peek(d -> d.setContrato(contract))
                    .collect(Collectors.toList());
            contract.setDevedores(debtors);
        }

        // Mapear garantidores
        if (data.garantidores() != null) {
            List<Guarantors> guarantors = data.garantidores().stream()
                    .map(this::toGuarantorEntity)
                    .peek(g -> g.setContrato(contract))
                    .collect(Collectors.toList());
            contract.setGarantidores(guarantors);
        }

        // Mapear serventia com verificação de existência
        if (data.serventia() != null && hasValidNotaryData(data.serventia())) {
            Notary notary;
            if (contract.getId() != null) {
                notary = notaryRepository.findByContratoId(contract.getId())
                        .orElseGet(() -> toNotaryEntity(data.serventia()));
            } else {
                notary = toNotaryEntity(data.serventia());
            }

            // Atualizar campos
            notary.setCns(data.serventia().cns() != null ? data.serventia().cns().toString() : null);
            notary.setName(data.serventia().nome());
            notary.setHolder(data.serventia().titular());
            notary.setSubstitute(data.serventia().substituto());
            notary.setAddress(data.serventia().endereco());
            notary.setContactPhone(data.serventia().telefoneContato());
            notary.setContrato(contract);
            contract.setServentia(notary);
        }

        // Mapear detran com verificação de existência
        if (data.detran() != null) {
            Detran detran;
            if (contract.getId() != null) {
                detran = detranRepository.findByContratoId(contract.getId())
                        .orElseGet(() -> toDetranEntity(data.detran()));
            } else {
                detran = toDetranEntity(data.detran());
            }

            // Atualizar campos
            detran.setSiglaUf(data.detran().siglaUf());
            detran.setNomeDetran(data.detran().nomeDetran());
            detran.setEstado(data.detran().estado());
            detran.setContrato(contract);
            contract.setDetran(detran);
        }

        // Mapear órgãos (agencies)
        List<Agency> agencies = new ArrayList<>();
        agencies.addAll(mapAgencies(data.orgaoEscob(), AgencyType.ESCOB, contract));
        agencies.addAll(mapAgencies(data.orgaoGuincho(), AgencyType.GUINCHO, contract));
        agencies.addAll(mapAgencies(data.orgaoDespachante(), AgencyType.DESPACHANTE, contract));
        agencies.addAll(mapAgencies(data.orgaoLeilao(), AgencyType.LEILAO, contract));
        agencies.addAll(mapAgencies(data.orgaoLocalizador(), AgencyType.LOCALIZADOR, contract));
        agencies.addAll(mapAgencies(data.orgaoPatio(), AgencyType.PATIO, contract));
        contract.setOrgaos(agencies);

        // Mapear notificações
        List<Notification> notifications = new ArrayList<>();
        notifications.addAll(mapNotifications(data.notificacaoEletronica(), "ELETRONICA", contract));
        notifications.addAll(mapNotifications(data.notificacaoViaAr(), "VIA_AR", contract));
        contract.setNotificacoes(notifications);

        if (data.veiculos() != null) {
            processVehicles(contract, existingVehicles != null ? existingVehicles : new ArrayList<>(), data.veiculos());
        } else if (existingVehicles != null && !existingVehicles.isEmpty()) {
            // Se não há veículos na API, usar os existentes
            existingVehicles.forEach(v -> v.setContratoEntity(contract));
            contract.setVeiculos(existingVehicles);
        }

        return contract;
    }

    private boolean hasValidNotaryData(QueryDetailResponseDTO.Serventia serventia) {
        return serventia.cns() != null ||
                (serventia.nome() != null && !serventia.nome().trim().isEmpty());
    }

    private Creditor toCreditorEntity(QueryDetailResponseDTO.Credor dto) {
        Creditor entity = new Creditor();
        entity.setName(dto.nome());
        entity.setCnpj(dto.cnpj());
        entity.setEmail(dto.email());
        entity.setStateRegistration(dto.inscricaoEstadual());
        entity.setAddress(dto.endereco());
        entity.setPhone(dto.telefone());
        return entity;
    }

    private Debtor toDebtorEntity(QueryDetailResponseDTO.Devedor dto) {
        if (dto == null || dto.cpfCnpj() == null) {
            return null;
        }

        // Verificar se já existe Debtor com esse cpfCnpj
        Optional<Debtor> existingDebtor = debtorRepository.findByCpfCnpj(dto.cpfCnpj());

        Debtor entity;
        if (existingDebtor.isPresent()) {
            entity = existingDebtor.get();
            // Atualize campos se desejar, por exemplo nome, email, telefone
            entity.setName(dto.nome());
        } else {
            entity = new Debtor();
            entity.setCpfCnpj(dto.cpfCnpj());
            entity.setName(dto.nome());
        }

        // Atualizar email
        if (dto.contatosEmail() != null && !dto.contatosEmail().isEmpty()) {
            entity.setEmail(dto.contatosEmail().get(0).email());
        }

        // Atualizar telefone
        if (dto.contatosTelefone() != null && !dto.contatosTelefone().isEmpty()) {
            entity.setCellPhone(dto.contatosTelefone().get(0).telefone());
        }

        // Processar endereço — buscar existente antes de salvar
        if (dto.enderecos() != null && !dto.enderecos().isEmpty()) {
            String enderecoStr = dto.enderecos().get(0).endereco();
            Address address = addressParser.parseAddress(enderecoStr);

            Optional<Address> existingAddress = addressRepository
                    .findByPostalCodeAndStreetAndNumberAndNeighborhoodAndCity(
                            address.getPostalCode(),
                            address.getStreet(),
                            address.getNumber(),
                            address.getNeighborhood(),
                            address.getCity()
                    );

            if (existingAddress.isPresent()) {
                entity.setAddress(existingAddress.get());
            } else {
                Address savedAddress = addressRepository.save(address);
                entity.setAddress(savedAddress);
            }
        }

        return entity;
    }

    private Guarantors toGuarantorEntity(QueryDetailResponseDTO.Garantidor dto) {
        Guarantors entity = new Guarantors();
        entity.setNome(dto.nome());
        entity.setCpfCnpj(dto.cpfCnpj());
        return entity;
    }

    private Notary toNotaryEntity(QueryDetailResponseDTO.Serventia dto) {
        Notary entity = new Notary();
        entity.setCns(dto.cns() != null ? dto.cns().toString() : null);
        entity.setName(dto.nome());
        entity.setHolder(dto.titular());
        entity.setSubstitute(dto.substituto());
        entity.setAddress(dto.endereco());
        entity.setContactPhone(dto.telefoneContato());
        return entity;
    }

    private Detran toDetranEntity(QueryDetailResponseDTO.Detran dto) {
        Detran entity = new Detran();
        entity.setSiglaUf(dto.siglaUf());
        entity.setNomeDetran(dto.nomeDetran());
        entity.setEstado(dto.estado());
        return entity;
    }

    private List<Agency> mapAgencies(List<QueryDetailResponseDTO.Orgao> orgaos, AgencyType type, Contract contract) {
        if (orgaos == null) {
            return new ArrayList<>();
        }

        return orgaos.stream()
                .map(orgao -> {
                    Agency entity = new Agency();
                    entity.setName(orgao.nome());
                    entity.setCnpj(orgao.cnpj());
                    entity.setAddress(orgao.endereco());
                    entity.setEmail(orgao.email());
                    entity.setContactPhone(orgao.telefoneContato());
                    entity.setType(type);
                    entity.setContrato(contract);
                    return entity;
                })
                .collect(Collectors.toList());
    }

    private List<Notification> mapNotifications(List<QueryDetailResponseDTO.Notificacao> notificacoes,
                                                String type, Contract contract) {
        if (notificacoes == null) {
            return new ArrayList<>();
        }

        return notificacoes.stream()
                .map(notificacao -> {
                    Notification entity = new Notification();
                    entity.setCommunicationMethod(notificacao.formaComunicacao());
                    entity.setSentDate(notificacao.dataEnvio());
                    entity.setReadDate(notificacao.dataLeitura());
                    entity.setEvidenceFileBase64(notificacao.arquivoEvidencia());
                    entity.setContrato(contract);
                    return entity;
                })
                .collect(Collectors.toList());
    }

    public void processVehicles(Contract contract, List<VehicleCache> existingVehicles, List<QueryDetailResponseDTO.Veiculo> apiVehicles) {
        if (apiVehicles == null || apiVehicles.isEmpty()) return;

        // APENAS atualize os existentes - não crie novos
        for (VehicleCache existingVehicle : existingVehicles) {
            apiVehicles.stream()
                    .filter(apiVehicle -> apiVehicle.placa() != null && apiVehicle.placa().equals(existingVehicle.getPlaca()))
                    .findFirst()
                    .ifPresent(apiVehicle -> {
                        updateVehicleFromApi(existingVehicle, apiVehicle);
                        existingVehicle.setContratoEntity(contract);
                    });
        }
    }

    private void updateVehicleFromApi(VehicleCache vehicle, QueryDetailResponseDTO.Veiculo apiVehicle) {
        vehicle.setMarcaModelo(apiVehicle.marcaModelo());
        vehicle.setRegistroDetran(apiVehicle.registroDetran());
        vehicle.setPossuiGPS(Boolean.valueOf(apiVehicle.possuiGps()));
        vehicle.setAnoFabricacao(apiVehicle.anoFabricacao());
        vehicle.setAnoModelo(apiVehicle.anoModelo());
        vehicle.setCor(apiVehicle.cor());
        vehicle.setChassi(apiVehicle.chassi());
        vehicle.setRenavam(apiVehicle.renavam());
        vehicle.setGravame(apiVehicle.gravame() != null ? apiVehicle.gravame().toString() : null);
        // Atualize outros campos conforme necessário
    }

}