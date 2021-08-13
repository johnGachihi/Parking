package com.johngachihi.parking.e2e

import com.digitalpetri.modbus.ExceptionCode
import com.digitalpetri.modbus.codec.ModbusTcpPayload
import com.digitalpetri.modbus.responses.ExceptionResponse
import com.johngachihi.parking.entities.visit.FinishedVisit
import com.johngachihi.parking.make8Byte_ByteBufFromLong_LE
import com.johngachihi.parking.modbustcp.camel.ModbusTcpEndpointProperties
import com.johngachihi.parking.modbustcp.makeWriteMultipleRegisterModbusRequestMessage
import com.johngachihi.parking.repositories.visit.OngoingVisitRepository
import io.netty.buffer.ByteBuf
import org.apache.camel.ProducerTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import javax.persistence.EntityManager

@SpringBootTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
    ]
)
@AutoConfigureTestDatabase
class EndToEnd {
    @Autowired
    private lateinit var producer: ProducerTemplate

    @Autowired
    private lateinit var modbusEndpointProps: ModbusTcpEndpointProperties

    @Autowired
    private lateinit var ongoingVisitRepository: OngoingVisitRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    private val modbusTcpEndpoint by lazy {
        "netty:tcp://${modbusEndpointProps.address}:${modbusEndpointProps.port}" +
                "?decoders=modbusTcpClientCodecFactory" +
                "&requestTimeout=5000"
    }

    @AfterEach
    fun clearDB() {
        ongoingVisitRepository.deleteAll()
    }

    @Test
    fun `When entry request received, then inserts visit data to DB`() {
        val ticketCode = 1234L
        makeEntryRequest(ticketCode)

        assertThat(ongoingVisitRepository.count())
            .isEqualTo(1)

        assertThat(ongoingVisitRepository.existsByTicketCode(ticketCode))
            .isTrue()
    }

    @Test
    @DisplayName(
        "When entry is attempted with a ticket-code that is in use, " +
                "then returns an InvalidDataValue exceptional modbus response"
    )
    fun testWhenEntryAttemptWithInUseTicketCode() {
        val ticketCodeInUse = 123455L
        makeEntryRequest(ticketCodeInUse)

        val response = makeEntryRequest(ticketCodeInUse)

        assertIsIllegalDataValueExceptionalResponse(response)
    }

    @Test
    @DisplayName(
        "When exit attempt is made with a ticket-code that is not in use, " +
                "then returns an InvalidDataValue exceptional modbus response"
    )
    fun testWhenExitAttemptWithNotInUseTicketCode() {
        val response = makeExitRequest(1234)

        assertIsIllegalDataValueExceptionalResponse(response)
    }

    @Test
    @DisplayName(
        "When exit attempt is made for an OngoingVisit with a ticket-code that is in use, " +
                "then the OngoingVisit is made a FinishedVisit"
    )
    fun testWhenExitAttemptWithInUseTicketCode() {
        val ticketCodeInUse = 11222L
        makeEntryRequest(ticketCodeInUse)

        makeExitRequest(ticketCodeInUse)

        assertThatASingleFinishedVisitExistsWithTicketCode(ticketCodeInUse)
    }


    private fun makeEntryRequest(ticketCode: Long): ModbusTcpPayload {
        return makeModbusRequest(
            address = 1,
            quantity = 4,
            writeData = make8Byte_ByteBufFromLong_LE(ticketCode)
        )
    }

    private fun makeExitRequest(ticketCode: Long): ModbusTcpPayload {
        return makeModbusRequest(
            address = 2,
            quantity = 4,
            writeData = make8Byte_ByteBufFromLong_LE(ticketCode)
        )
    }

    private fun makeModbusRequest(address: Int, quantity: Int, writeData: ByteBuf): ModbusTcpPayload {
        val request = makeWriteMultipleRegisterModbusRequestMessage(
            address = address,
            quantity = quantity,
            writeData = writeData
        )
        val response = producer.requestBody(modbusTcpEndpoint, request)

        assertThat(response)
            .isInstanceOf(ModbusTcpPayload::class.java)
            .withFailMessage {
                """The Modbus entry request expected a ModbusTcpPayload response.
                    |Received a response of type ${response::class.simpleName}
                """.trimMargin()
            }

        return response as ModbusTcpPayload
    }

    private fun assertIsIllegalDataValueExceptionalResponse(response: ModbusTcpPayload) {
        val failMessage = "The response is not a Modbus IllegalDataValue exceptional response"

        assertThat(response.modbusPdu)
            .withFailMessage(failMessage)
            .isInstanceOf(ExceptionResponse::class.java)

        val exceptionPdu = response.modbusPdu as ExceptionResponse
        assertThat(exceptionPdu.exceptionCode)
            .withFailMessage(failMessage)
            .isEqualTo(ExceptionCode.IllegalDataValue)
    }

    private fun assertThatASingleFinishedVisitExistsWithTicketCode(ticketCode: Long) {
        val finishedVisits = entityManager.createQuery(
            "select f from FinishedVisit f where f.ticketCode = :ticketCode",
            FinishedVisit::class.java
        )
            .setParameter("ticketCode", ticketCode)
            .resultList

        assertThat(finishedVisits.size)
            .withFailMessage("The number of FinishedVisits with the ticketCode $ticketCode " +
                    "is ${finishedVisits.size} instead of one.")
            .isEqualTo(1)

    }
}