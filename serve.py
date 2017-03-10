#!/usr/bin/env python3
"""simple dns server."""

import asyncio
import signal

import requests
import dns.message
import dns.rcode
import dns.rrset

s = requests.Session()  # use connections pool


def resolve(dns_name, dns_type, edns=None):
    payload = {
        "name": dns_name,
        "type": dns_type,
    }
    if edns is not None:
        payload['edns_client_subnet'] = edns
    r = s.get('https://dns.google.com/resolve', params=payload)
    return r.json()


class DomainNameServerProtocol:
    def connection_made(self, transport):
        print('start', transport)
        self.transport = transport

    def datagram_received(self, data, addr):
        query = dns.message.from_wire(data)  # get query message

        print(query.question[0].to_text())
        r = resolve(query.question[0].name.to_text(), query.question[0].rdtype)

        resp = dns.message.make_response(query)
        resp.set_rcode(r['Status'])
        if 'Answer' in r:
            for answer in r['Answer']:
                rrset = dns.rrset.from_text(answer['name'], answer['TTL'],
                                            dns.rdataclass.IN, answer['type'],
                                            answer['data'])
                resp.answer.append(rrset)
        if 'Authority' in r:
            for answer in r['Authority']:
                rrset = dns.rrset.from_text(answer['name'], answer['TTL'],
                                            dns.rdataclass.IN, answer['type'],
                                            answer['data'])
                resp.authority.append(rrset)

        self.transport.sendto(resp.to_wire(), addr)

    def error_received(self, exc):
        print('Error received:', exc)

    def connection_lost(self, exc):
        print('stop', exc)


def start_server(loop, addr):
    t = asyncio.Task(
        loop.create_datagram_endpoint(
            DomainNameServerProtocol, local_addr=addr))
    transport, server = loop.run_until_complete(t)
    return transport


if __name__ == '__main__':
    # exit the program when catch the KeyboardInterrupt
    signal.signal(signal.SIGINT, signal.SIG_DFL)

    loop = asyncio.get_event_loop()
    server = start_server(loop, ('127.0.0.1', '53'))

    try:
        loop.run_forever()
    finally:
        server.close()  #
